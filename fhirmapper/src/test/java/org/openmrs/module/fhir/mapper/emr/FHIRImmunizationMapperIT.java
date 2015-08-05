package org.openmrs.module.fhir.mapper.emr;

import org.hl7.fhir.instance.formats.ParserBase;
import org.hl7.fhir.instance.model.AtomFeed;
import org.hl7.fhir.instance.model.Resource;
import org.hl7.fhir.instance.model.ResourceType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.module.fhir.MapperTestHelper;
import org.openmrs.module.fhir.ObsHelper;
import org.openmrs.module.fhir.utils.FHIRFeedHelper;
import org.openmrs.module.fhir.utils.GlobalPropertyLookUpService;
import org.openmrs.module.shrclient.util.ConceptCache;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;

import java.util.*;

import static org.junit.Assert.*;
import static org.openmrs.module.fhir.mapper.MRSProperties.*;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FHIRImmunizationMapperIT extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private ApplicationContext springContext;

    @Autowired
    FHIRImmunizationMapper mapper;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private GlobalPropertyLookUpService globalPropertyLookUpService;

    private Resource resource;
    private AtomFeed feed;
    private ObsHelper obsHelper;


    @Before
    public void setUp() throws Exception {
        executeDataSet("testDataSets/immunizationDS.xml");

        ParserBase.ResourceOrFeed resourceOrFeed = new MapperTestHelper().loadSampleFHIREncounter("encounterBundles/encounterWithImmunization.xml", springContext);
        feed = resourceOrFeed.getFeed();
        resource = FHIRFeedHelper.identifyResource(feed.getEntryList(), ResourceType.Immunization);
        obsHelper = new ObsHelper();
    }


    @Test
    public void shouldHandleResourceOfTypeImmnunization() throws Exception {
        assertTrue(mapper.canHandle(resource));
    }

    @Test
    public void shouldMapDateOfVaccination() throws Exception {
        Obs obs = mapImmunizationIncidentObs();
        DateTime dateTime = new DateTime(DateTimeZone.forTimeZone(TimeZone.getTimeZone("IST")));
        Date expectedDate = dateTime.withDate(2014, 12, 3).toDateMidnight().toDate();

        Obs vaccineDateObs = obsHelper.findMemberObsByConceptName(obs, MRS_CONCEPT_VACCINATION_DATE, globalPropertyLookUpService);
        assertEquals(expectedDate, vaccineDateObs.getValueDate());
    }

    @Test
    public void shouldMapVaccinationReported() throws Exception {
        Obs obs = mapImmunizationIncidentObs();

        Obs vaccinationReported = obsHelper.findMemberObsByConceptName(obs, MRS_CONCEPT_VACCINATION_REPORTED, globalPropertyLookUpService);
        assertTrue(vaccinationReported.getValueAsBoolean());
    }

    @Test
    public void shouldMapRefusedIndicator() throws Exception {
        Obs obs = mapImmunizationIncidentObs();
        Obs refusedIndicator = obsHelper.findMemberObsByConceptName(obs, MRS_CONCEPT_VACCINATION_REFUSED, globalPropertyLookUpService);
        assertFalse(refusedIndicator.getValueAsBoolean());

    }

    @Test
    public void shouldMapQuantityUnits() throws Exception {
        Obs obs = mapImmunizationIncidentObs();
        Obs quantityUnits = obsHelper.findMemberObsByConceptName(obs, VALUESET_QUANTITY_UNITS, globalPropertyLookUpService);

        int mgConceptCode = 50;
        assertEquals(quantityUnits.getValueCoded(), conceptService.getConcept(mgConceptCode));
    }

    @Test
    public void shouldMapDosage() throws Exception {
        Obs obs = mapImmunizationIncidentObs();
        Obs dosage = obsHelper.findMemberObsByConceptName(obs, MRS_CONCEPT_DOSAGE, globalPropertyLookUpService);

        assertTrue(dosage.getValueNumeric() == 100.0);
    }

    @Test
    public void shouldMapVaccineType() throws Exception {
        Obs obs = mapImmunizationIncidentObs();
        Obs vaccineType = obsHelper.findMemberObsByConceptName(obs, MRS_CONCEPT_VACCINE, globalPropertyLookUpService);

        int paracetemol500 = 550;
        assertEquals(vaccineType.getValueCoded(), conceptService.getConcept(paracetemol500));


    }

    @Test
    public void shouldMapImmunizationReason() throws Exception {
        Obs obs = mapImmunizationIncidentObs();
        Obs immunizationReason = obsHelper.findMemberObsByConceptName(obs, VALUESET_IMMUNIZATION_REASON, globalPropertyLookUpService);

        int travelVaccination = 501;
        assertEquals(immunizationReason.getValueCoded(), conceptService.getConcept(travelVaccination));
    }

    @Test
    public void shouldMapImmunizationRefusalReason() throws Exception {
        Obs obs = mapImmunizationIncidentObs();
        Obs immunizationReason = obsHelper.findMemberObsByConceptName(obs, VALUESET_IMMUNIZATION_REFUSAL_REASON, globalPropertyLookUpService);

        int patientObjection = 502;
        assertEquals(immunizationReason.getValueCoded(), conceptService.getConcept(patientObjection));
    }

    @Test
    public void shouldMapRoute() throws Exception {
        Obs obs = mapImmunizationIncidentObs();
        Obs route = obsHelper.findMemberObsByConceptName(obs, VALUESET_ROUTE, globalPropertyLookUpService);

        int oral = 503;
        assertEquals(route.getValueCoded(), conceptService.getConcept(oral));


    }

    private Obs mapImmunizationIncidentObs() {
        Encounter mrsEncounter = new Encounter();
        ConceptCache conceptCache = new ConceptCache(conceptService, globalPropertyLookUpService);
        mapper.map(feed, resource, null, mrsEncounter, new HashMap<String, List<String>>(), conceptCache);

        Set<Obs> allObs = mrsEncounter.getAllObs();
        assertEquals(1, allObs.size());
        Obs obs = allObs.iterator().next();
        assertEquals(MRS_CONCEPT_IMMUNIZATION_INCIDENT, obs.getConcept().getName().getName());

        return obs;
    }
}