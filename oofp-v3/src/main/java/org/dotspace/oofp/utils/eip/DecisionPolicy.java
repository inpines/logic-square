package org.dotspace.oofp.utils.eip;

import org.dotspace.oofp.model.dto.eip.ControlDecision;
import org.dotspace.oofp.model.dto.eip.InboundDecisionView;

@FunctionalInterface
public interface DecisionPolicy {

    ControlDecision decide(InboundDecisionView decisionView);

}
