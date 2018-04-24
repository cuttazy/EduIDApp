package com.eduid.EduIdApp.controller.ServiceManagement;

import android.content.Context;

import com.eduid.EduIdApp.controller.ActivitiesManager;
import com.eduid.EduIdApp.controller.Config;
import com.eduid.EduIdApp.model.EduIdDB;
import com.eduid.EduIdApp.model.dataobjects.EduIDService;

import java.util.ArrayList;

/**
 * Created by Yann Cuttaz on 08.02.17.
 */
public class AssertionManager {

    private Context currentContext;
    private AssertionCallback callback;
    private Integer numberOfTasksToDo;
    private ArrayList<EduIDService> authorizedServices = new ArrayList<>();

    public AssertionManager(Context ctx){
        this.currentContext = ctx;
    }

    /**
     * Get Grant token and save it locally
     * @return
     */
    public void doAssertion(ArrayList<EduIDService> services, AssertionCallback assertionCallback){

        /**
         * Local params
         */
        this.callback = assertionCallback;
        this.numberOfTasksToDo = services.size();

        Config.debug("number of services selected: " + services.size());

        /**
         * Task for each platforms
         */
        for (EduIDService service :
                services) {

            AssertionTask task = new AssertionTask();
            task.context = currentContext;
            task.federationServiceURL = service.getHomePageLink() + service.getApis().get(0).getApiLink();
            task.homePageLink = service.getHomePageLink();
            task.service = service;

            Config.debug(service.toJSON().toString());
            Config.debug(task.federationServiceURL);
            task.callback = new AssertionTask.AssertionTaskCallback() {


                @Override
                public void onTaskFinish(EduIDService service, String jsonResponse) {

                    service.setAuthorization(jsonResponse);
                    authorizedServices.add(service);

                    synchronized (numberOfTasksToDo) { // not change number of assertions before finish current process
                        numberOfTasksToDo--;
                        if (numberOfTasksToDo == 0 && AssertionManager.this.callback != null) { // Check if all retrieve assertions are finished
                            AssertionManager.this.callback.onAssertionFinish(authorizedServices); // All are saved
                        }
                    }

                }

                @Override
                public void onTaskError(EduIDService service, boolean logout) {
                    synchronized (numberOfTasksToDo) { // not change number of assertions before finish current process
                        numberOfTasksToDo = 0;
                        if(AssertionManager.this.callback != null) {
                            AssertionManager.this.callback.onAssertionError("Assertion error on a service. Check selection and try again.");
                            AssertionManager.this.callback = null;
                        }
                    }
                    if(logout){
                        new EduIdDB(currentContext).logout();
                        ActivitiesManager.startLoginActivity(currentContext);
                    }
                }
            };
            task.execute();
        }

    }

    public interface AssertionCallback{
        void onAssertionFinish(ArrayList<EduIDService> authorizedServices);
        void onAssertionError(String errorMessage);
    }


}
