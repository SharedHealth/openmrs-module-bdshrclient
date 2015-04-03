package org.openmrs.module.shrclient.mapper;

import org.openmrs.PersonAttribute;
import org.openmrs.module.fhir.utils.DateUtil;
import org.openmrs.module.shrclient.model.Patient;
import org.openmrs.module.shrclient.model.Status;
import org.openmrs.module.shrclient.service.BbsCodeService;
import org.openmrs.module.shrclient.util.AddressHelper;


import static org.openmrs.module.fhir.utils.Constants.*;

public class PatientMapper {
    private BbsCodeService bbsCodeService;
    private final AddressHelper addressHelper;

    public PatientMapper(BbsCodeService bbsCodeService) {
        this.bbsCodeService = bbsCodeService;
        this.addressHelper = new AddressHelper();
    }

    public PatientMapper(BbsCodeService bbsCodeService, AddressHelper addressHelper) {
        this.bbsCodeService = bbsCodeService;
        this.addressHelper = addressHelper;
    }

    public Patient map(org.openmrs.Patient openMrsPatient) {
        Patient patient = new Patient();

        String nationalId = getAttributeValue(openMrsPatient, NATIONAL_ID_ATTRIBUTE);
        if (nationalId != null) {
            patient.setNationalId(nationalId);
        }

        String healthId = getAttributeValue(openMrsPatient, HEALTH_ID_ATTRIBUTE);
        if (healthId != null) {
            patient.setHealthId(healthId);
        }

        String birthRegNo = getAttributeValue(openMrsPatient, BIRTH_REG_NO_ATTRIBUTE);
        if (birthRegNo != null) {
            patient.setBirthRegNumber(birthRegNo);
        }

        String uniqueId = getAttributeValue(openMrsPatient, UNIQUE_ID_ATTRIBUTE);
        if (uniqueId != null) {
            patient.setUniqueId(uniqueId);
        }

        patient.setGivenName(openMrsPatient.getGivenName());
        patient.setSurName(openMrsPatient.getFamilyName());
        patient.setGender(openMrsPatient.getGender());
        patient.setDateOfBirth(DateUtil.toDateString(openMrsPatient.getBirthdate(), DateUtil.ISO_DATE_IN_HOUR_MIN_FORMAT));

        PersonAttribute occupation = getAttribute(openMrsPatient, OCCUPATION_ATTRIBUTE);
        if (occupation != null) {
            patient.setOccupation(bbsCodeService.getOccupationCode(occupation.toString()));
        }

        PersonAttribute education = getAttribute(openMrsPatient, EDUCATION_ATTRIBUTE);
        if (education != null) {
            patient.setEducationLevel(bbsCodeService.getEducationCode(education.toString()));
        }

        String primaryContact = getAttributeValue(openMrsPatient, PRIMARY_CONTACT_ATTRIBUTE);
        if (primaryContact != null) {
            patient.setPrimaryContact(primaryContact);
        }
        patient.setAddress(addressHelper.getMciAddress(openMrsPatient));
        patient.setStatus(getMciPatientStatus(openMrsPatient));
        return patient;
    }

    private String getAttributeValue(org.openmrs.Patient openMrsPatient, String attributeName) {
        PersonAttribute attribute = getAttribute(openMrsPatient, attributeName);
        return attribute != null ? attribute.getValue() : null;
    }

    private PersonAttribute getAttribute(org.openmrs.Patient openMrsPatient, String attributeName) {
        return openMrsPatient.getAttribute(attributeName);
    }

    private Status getMciPatientStatus(org.openmrs.Patient openMrsPatient) {
        Status status = new Status();
        Character type = '1';
        String dateOfDeath = null;
        Boolean isDead = openMrsPatient.isDead();
        if (isDead) {
            type = '2';
        }
        if (openMrsPatient.getDeathDate() != null) {
            dateOfDeath = DateUtil.toDateString(openMrsPatient.getDeathDate(), DateUtil.ISO_DATE_IN_HOUR_MIN_FORMAT);
        }
        status.setType(type);
        status.setDateOfDeath(dateOfDeath);
        return status;
    }
}
