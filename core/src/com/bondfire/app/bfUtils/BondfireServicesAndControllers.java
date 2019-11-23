package com.bondfire.app.bfUtils;

import com.bondfire.app.callbacks.PlatformInterfaceController;
import com.bondfire.app.services.AdController;
import com.bondfire.app.services.PlayServicesObject;
import com.bondfire.app.services.RealTimeMultiplayerService;
import com.bondfire.app.services.TurnBasedMultiplayerService;

/**
 * Created by alvaregd on 17/05/16.
 * Provides methods for the platform to inject various services/controllers
 * to the games
 */
public interface BondfireServicesAndControllers {

    /** set the GoogleGamePlay Services object **/
    void setPlayServicesResources(PlayServicesObject group);

    /** controls interface for the banner add */
    void setAdController(AdController adController);

    /** control menu visibily of bondfire platform **/
    void setPlatformController(PlatformInterfaceController controller);

    /** access to the real time services provided by google **/
    void setRealTimeServices(RealTimeMultiplayerService realTimeServices);

    /** access to the turnbased services provided by google */
    void setTurnBasedServices(TurnBasedMultiplayerService turnBasedServices);

}
