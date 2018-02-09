package org.bahmni.module.urgencyButton;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.Order;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderPreSaveCommandImpl implements EncounterDataPreSaveCommand {
    private final String STAT = "STAT";
    private final String LAB_TEST = "LabTest";

    @Override
    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        List<Order> orders = bahmniEncounterTransaction.getOrders();
        for(Order order : orders) {
            String urgency = order.getUrgency();
            if(STAT.equals(urgency)) {
                String conceptUuid = order.getConceptUuid();
                Concept concept = Context.getConceptService().getConceptByUuid(conceptUuid);
                String orderType = concept.getConceptClass().getName();
                if(LAB_TEST.equals(orderType)) {
                    String priority = concept.getName().getName() +" - Priority";
                    String comments = order.getCommentToFulfiller();
                    comments = StringUtils.isEmpty(comments) ? priority : priority.concat(", "+ comments);
                    order.setCommentToFulfiller(comments);
                }
            }
        }
        return bahmniEncounterTransaction;
    }


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
