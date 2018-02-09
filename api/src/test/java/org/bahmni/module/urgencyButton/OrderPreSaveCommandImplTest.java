package org.bahmni.module.urgencyButton;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.Concept;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.Order;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, OrderPreSaveCommandImpl.class })
public class OrderPreSaveCommandImplTest {

    @Mock
    private ConceptService conceptService;

    @Mock
    private org.openmrs.Concept concept;

    @Mock
    private ConceptClass conceptClass;

    @Mock
    private ConceptName conceptName;

    private BahmniEncounterTransaction bahmniEncounterTransaction;
    private OrderPreSaveCommandImpl orderPreSaveCommand = new OrderPreSaveCommandImpl();
    private final String STAT = "STAT";

    @Test
    public void shouldNotDoAnyChangesToBahmniEncounterIfThereAreNoOrders() {
        bahmniEncounterTransaction = new BahmniEncounterTransaction();
        BahmniEncounterTransaction bahmniEncounter = orderPreSaveCommand.update(bahmniEncounterTransaction);

        Assert.assertEquals(bahmniEncounterTransaction, bahmniEncounter);
    }

    @Test
    public void shouldNotDoAnyChangesWhenNoOrderIsPriority() {
        bahmniEncounterTransaction = getBahmniTransaction("");

        BahmniEncounterTransaction actual = orderPreSaveCommand.update(bahmniEncounterTransaction);

        Assert.assertEquals(bahmniEncounterTransaction, actual);
    }

    @Test
    public void shouldNotDoAnyChangesWhenOrdersArePriorityButNotLabOrders() {
        bahmniEncounterTransaction = getBahmniTransaction("");

        setUpMocks();
        when(conceptClass.getName()).thenReturn("Radiology");

        BahmniEncounterTransaction actual = orderPreSaveCommand.update(bahmniEncounterTransaction);

        Assert.assertEquals(bahmniEncounterTransaction, actual);
    }

    @Test
    public void shouldAddPriorityWithTestNameToNotesWhenLabTestHaveUrgency() {
        bahmniEncounterTransaction = getBahmniTransaction("FIRST ORDER");

        setUpMocks();
        when(conceptClass.getName()).thenReturn("LabTest");
        when(conceptName.getName()).thenReturn("Microscopy");

        BahmniEncounterTransaction actual = orderPreSaveCommand.update(bahmniEncounterTransaction);

        verifyStatic(VerificationModeFactory.times(1));
        Context.getConceptService();
        verify(conceptService, times(1)).getConceptByUuid("e39a473c-a57a-4b29-b5ba-b02832c17b35");
        verify(concept, times(1)).getConceptClass();
        verify(conceptClass, times(1)).getName();
        verify(concept, times(1)).getName();
        verify(conceptName, times(1)).getName();

        Assert.assertEquals("Microscopy - Priority", actual.getOrders().get(0).getCommentToFulfiller());
        Assert.assertEquals("Notes", actual.getOrders().get(1).getCommentToFulfiller());
    }

    @Test
    public void shouldAppendPriorityTextToTheExistingNotes() {
        bahmniEncounterTransaction = getBahmniTransaction("BOTH");

        setUpMocks();
        when(conceptClass.getName()).thenReturn("LabTest");
        when(conceptName.getName())
                .thenReturn("Microscopy")
                .thenReturn("Gram Stain");

        BahmniEncounterTransaction actual = orderPreSaveCommand.update(bahmniEncounterTransaction);

        verifyStatic(VerificationModeFactory.times(2));
        Context.getConceptService();
        verify(conceptService, times(1)).getConceptByUuid("e39a473c-a57a-4b29-b5ba-b02832c17b35");
        verify(conceptService, times(1)).getConceptByUuid("e39a473c-a57a-4b29-b5ba-b02832c13der");
        verify(concept, times(2)).getConceptClass();
        verify(conceptClass, times(2)).getName();
        verify(concept, times(2)).getName();
        verify(conceptName, times(2)).getName();

        Assert.assertEquals("Microscopy - Priority", actual.getOrders().get(0).getCommentToFulfiller());
        Assert.assertEquals("Gram Stain - Priority, Notes", actual.getOrders().get(1).getCommentToFulfiller());
    }

    private void setUpMocks() {
        mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        when(conceptService.getConceptByUuid("e39a473c-a57a-4b29-b5ba-b02832c17b35")).thenReturn(concept);
        when(conceptService.getConceptByUuid("e39a473c-a57a-4b29-b5ba-b02832c13der")).thenReturn(concept);
        when(concept.getConceptClass()).thenReturn(conceptClass);
        when(concept.getName()).thenReturn(conceptName);
    }

    private BahmniEncounterTransaction getBahmniTransaction(String addStatToOrder) {
        bahmniEncounterTransaction = new BahmniEncounterTransaction();
        Concept conceptOne = new Concept();
        Concept conceptTwo = new Concept();
        conceptOne.setUuid("e39a473c-a57a-4b29-b5ba-b02832c17b35");
        conceptTwo.setUuid("e39a473c-a57a-4b29-b5ba-b02832c13der");
        Order orderOne = new Order();
        Order orderTwo = new Order();
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
