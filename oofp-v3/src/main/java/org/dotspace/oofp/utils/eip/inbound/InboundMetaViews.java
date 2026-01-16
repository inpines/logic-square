package org.dotspace.oofp.utils.eip.inbound;

import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.model.dto.eip.InboundAttrKeys;
import org.dotspace.oofp.model.dto.eip.InboundMetaView;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class InboundMetaViews {

    /** 從 StepContext 取出 META；沒有就給空 map（policy 端不需要處理 null） */
    public InboundMetaView of(Map<String, String> meta) {
        return InboundMetaView.of(meta);
    }

    public <T> InboundMetaView from(StepContext<T> stepContext) {
        return InboundAttrKeys.META.maybe(stepContext)
                .map(InboundMetaView::of)
                .orElseGet(() -> InboundMetaView.of(Map.of()));
    }
}
