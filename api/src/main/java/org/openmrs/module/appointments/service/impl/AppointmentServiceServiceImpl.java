package org.openmrs.module.appointments.service.impl;

import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.dao.AppointmentDao;
import org.openmrs.module.appointments.dao.AppointmentServiceDao;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentService;
import org.openmrs.module.appointments.model.AppointmentServiceType;
import org.openmrs.module.appointments.model.ServiceWeeklyAvailability;
import org.openmrs.module.appointments.service.AppointmentServiceService;
import org.openmrs.module.appointments.service.AppointmentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

@Service
public class AppointmentServiceServiceImpl implements AppointmentServiceService {

    @Autowired
    AppointmentServiceDao appointmentServiceDao;

    @Autowired
    AppointmentsService appointmentsService;

    @Override
    public AppointmentService save(AppointmentService appointmentService) {
        AppointmentService service = appointmentServiceDao.getNonVoidedAppointmentServiceByName(appointmentService.getName());
        if(service != null) {
            throw new RuntimeException("The service '" + appointmentService.getName() + "' is already present");
        }
        return appointmentServiceDao.save(appointmentService);
    }

    @Override
    public List<AppointmentService> getAllAppointmentServices(boolean includeVoided) {
        return appointmentServiceDao.getAllAppointmentServices(includeVoided);
    }

    @Override
    public AppointmentService getAppointmentServiceByUuid(String uuid) {
        return appointmentServiceDao.getAppointmentServiceByUuid(uuid);
    }

    @Override
    public AppointmentService voidAppointmentService(AppointmentService appointmentService, String voidReason) {
        List<Appointment> allFutureAppointmentsForService = appointmentsService.getAllFutureAppointmentsForService(appointmentService);
        if (allFutureAppointmentsForService.size() > 0) {
            throw new RuntimeException("Please cancel all future appointments for this service to proceed. After deleting this service, you will not be able to see any appointments for it");
        }
        setVoidInfoForAppointmentService(appointmentService, voidReason);
        return appointmentServiceDao.save(appointmentService);
    }

    private void setVoidInfoForAppointmentService(AppointmentService appointmentService, String voidReason) {
        setVoidInfoForService(appointmentService, voidReason);
        setVoidInfoForWeeklyAvailability(appointmentService, voidReason);
        setVoidInfoForServiceTypes(appointmentService, voidReason);
    }

    private void setVoidInfoForService(AppointmentService appointmentService, String voidReason) {
        appointmentService.setVoided(true);
        appointmentService.setDateVoided(new Date());
        appointmentService.setVoidedBy(Context.getAuthenticatedUser());
        appointmentService.setVoidReason(voidReason);
    }

    private void setVoidInfoForServiceTypes(AppointmentService appointmentService, String voidReason) {
        for (AppointmentServiceType appointmentServiceType : appointmentService.getServiceTypes()) {
            appointmentServiceType.setVoided(true);
            appointmentServiceType.setDateVoided(new Date());
            appointmentServiceType.setVoidedBy(Context.getAuthenticatedUser());
            appointmentServiceType.setVoidReason(voidReason);
        }
    }

    private void setVoidInfoForWeeklyAvailability(AppointmentService appointmentService, String voidReason) {
        for (ServiceWeeklyAvailability serviceWeeklyAvailability : appointmentService.getWeeklyAvailability()) {
            serviceWeeklyAvailability.setVoided(true);
            serviceWeeklyAvailability.setDateVoided(new Date());
            serviceWeeklyAvailability.setVoidedBy(Context.getAuthenticatedUser());
            serviceWeeklyAvailability.setVoidReason(voidReason);
        }
    }

}