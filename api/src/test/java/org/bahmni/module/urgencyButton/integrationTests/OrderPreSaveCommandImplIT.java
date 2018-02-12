package org.bahmni.module.urgencyButton.integrationTests;

import org.bahmni.module.urgencyButton.OrderPreSaveCommandImpl;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class OrderPreSaveCommandImplIT extends BaseModuleWebContextSensitiveTest {
    private final String STAT = "STAT";

    @Autowired
    private OrderPreSaveCommandImpl orderPreSaveCommand;

    @Test
    public void shouldAddUrgentTextToTheUrgentLabOrders() throws Exception {
        executeDataSet("conceptTestData.xml");
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniTransaction("FIRST ORDER");

        BahmniEncounterTransaction actual = orderPreSaveCommand.update(bahmniEncounterTransaction);

        Assert.assertEquals("Microscopy - Urgent", actual.getOrders().get(0).getCommentToFulfiller());
        Assert.assertEquals("Notes", actual.getOrders().get(1).getCommentToFulfiller());
    }

    @Test
        public void shouldAddUrgentTextAlongWithNotesToTheUrgentLabOrders() throws Exception {
            executeDataSet("conceptTestData.xml");
            BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniTransaction("BOTH");

            BahmniEncounterTransaction actual = orderPreSaveCommand.update(bahmniEncounterTransaction);

            Assert.assertEquals("Microscopy - Urgent", actual.getOrders().get(0).getCommentToFulfiller());
            Assert.assertEquals("Gram Stain - Urgent, Notes", actual.getOrders().get(1).getCommentToFulfiller());
        }

    private BahmniEncounterTransaction getBahmniTransaction(String addStatToOrder) {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        EncounterTransaction.Concept conceptOne = new EncounterTransaction.Concept();
        EncounterTransaction.Concept conceptTwo = new EncounterTransaction.Concept();
        conceptOne.setUuid("e39a473c-a57a-4b29-b5ba-b02832c17b35");
        conceptTwo.setUuid("e39a473c-a57a-4b29-b5ba-b02832c13der");
        EncounterTransaction.Order orderOne = new EncounterTransaction.Order();
        EncounterTransaction.Order orderTwo = new EncounterTransaction.Order();
        orderOne.setConcept(conceptOne);
        orderTwo.setConcept(conceptTwo);
        orderTwo.setCommentToFulfiller("Notes");

        if("FIRST ORDER".equals(addStatToOrder)) {
            orderOne.setUrgency(STAT);
        } else if("BOTH".equals(addStatToOrder)) {
            orderOne.setUrgency(STAT);
            orderTwo.setUrgency(STAT);
        }

        bahmniEncounterTransaction.setOrders(Arrays.asList(orderOne, orderTwo));

        return bahmniEncounterTransaction;
    }
}
