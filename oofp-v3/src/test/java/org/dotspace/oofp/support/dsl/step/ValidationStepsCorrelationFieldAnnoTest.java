package org.dotspace.oofp.support.dsl.step;

import org.dotspace.oofp.utils.dsl.BehaviorStep;
import org.dotspace.oofp.model.dto.behaviorstep.Violations;
import org.dotspace.oofp.model.dto.behaviorstep.StepContext;
import org.dotspace.oofp.support.expression.ExpressionEvaluation;
import org.dotspace.oofp.support.expression.ExpressionEvaluations;
import org.dotspace.oofp.support.validator.constraint.MandatoryField;
import org.dotspace.oofp.support.validator.constraint.MandatoryFieldCase;
import org.dotspace.oofp.utils.functional.monad.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ValidationStepsCorrelationFieldAnnoTest {

    @Mock
    private Validator validator;

    @Mock
    private ExpressionEvaluations expressionEvaluations;

    @Mock
    private StepContext<Object> stepContext;

    @Mock
    private ExpressionEvaluation evalTrue;        // parse("true") → true
    @Mock
    private ExpressionEvaluation evalFalse;       // valueTest 解析 → false
    @Mock
    private ExpressionEvaluation evalNameResolver; // parse("name") 取欄位值

    private RequireParametersSteps steps;

    @BeforeEach
    void setUp() {
        steps = new RequireParametersSteps(validator, expressionEvaluations);
//        when(validator.validate(any())).thenReturn(Collections.emptySet()); // 與關聯檢核無關，回空集合即可
        when(expressionEvaluations.evaluate("true")).thenReturn(evalTrue);
        when(evalTrue.getValue(any())).thenReturn(true); // 讓每個 case 的 when 都成立
    }

    /** Case A：present=true 且值缺漏(null) → 應產生「此欄位必須輸入」 */
    @Test
    void correlation_presentRequired_butMissing_producesMustInputViolation() {
        class RequiredNameDto {
            @MandatoryField(cases = { @MandatoryFieldCase(when = "true") })
            String name;
        }
        RequiredNameDto dto = new RequiredNameDto(); // name 預設 null
        when(stepContext.getPayload()).thenReturn(dto);

        // 反射取得欄位名 "name"，ValidationSteps 會呼叫 parse("name") 來評估值
        when(expressionEvaluations.evaluate("name")).thenReturn(evalNameResolver);
        when(evalNameResolver.getValue(dto)).thenReturn(null);

        BehaviorStep<Object> step = steps.correlationStep();
        Validation<Violations, StepContext<Object>> result = step.execute(stepContext);

        assertTrue(result.isInvalid());
        Violations v = result.error().orElseThrow();
        assertTrue(v.stream().anyMatch(gv ->
                "name".equals(gv.getValidationName()) &&
                        gv.getMessages().stream().anyMatch(m -> m.contains("此欄位必須輸入"))));
    }

    /** Case B：present=false 且值存在(非空字串) → 應產生「此欄位不可輸入」 */
    @Test
    void correlation_presentForbidden_butValueExists_producesMustNotInputViolation() {
        class ForbiddenNameDto {
            @MandatoryField(cases = { @MandatoryFieldCase(when = "true", present = false) })
            String name = "something";
        }
        ForbiddenNameDto dto = new ForbiddenNameDto();
        when(stepContext.getPayload()).thenReturn(dto);

        when(expressionEvaluations.evaluate("name")).thenReturn(evalNameResolver);
        when(evalNameResolver.getValue(dto)).thenReturn(dto.name);

        BehaviorStep<Object> step = steps.correlationStep();
        Validation<Violations, StepContext<Object>> result = step.execute(stepContext);

        assertTrue(result.isInvalid());
        Violations v = result.error().orElseThrow();
        assertTrue(v.stream().anyMatch(gv ->
                "name".equals(gv.getValidationName()) &&
                        gv.getMessages().stream().anyMatch(m -> m.contains("此欄位不可輸入"))));
    }

    /** Case C：present=true 值存在，但 valueTest 失敗 → 應產生「欄位內容檢核失敗」 */
    @Test
    void correlation_valueTestFails_producesValidationFailed() {
        class ValueTestNameDto {
            @MandatoryField(cases = {
                    @MandatoryFieldCase(when = "true", valueTest = "test($$)")
            })
            String name = "abc";
        }
        ValueTestNameDto dto = new ValueTestNameDto();
        when(stepContext.getPayload()).thenReturn(dto);

        // 欄位值解析
        when(expressionEvaluations.evaluate("name")).thenReturn(evalNameResolver);
        when(evalNameResolver.getValue(dto)).thenReturn(dto.name);

        // valueTest：$$ → 欄位名 "name" → "test(name)"
        when(expressionEvaluations.evaluate("test(name)")).thenReturn(evalFalse);
        when(evalFalse.getValue(dto)).thenReturn(false); // 檢核失敗

        BehaviorStep<Object> step = steps.correlationStep();
        Validation<Violations, StepContext<Object>> result = step.execute(stepContext);

        assertTrue(result.isInvalid());
        Violations v = result.error().orElseThrow();
        assertTrue(v.stream().anyMatch(gv ->
                "name".equals(gv.getValidationName()) &&
                        gv.getMessages().stream().anyMatch(m -> m.contains("欄位內容檢核失敗"))));
    }

    /** Case D：值為 ""（空字串）→ isPresent 應判為 false（等同未輸入）→ 觸發「此欄位必須輸入」 */
    @Test
    void correlation_emptyString_isNotPresent_triggersMustInput() {
        class EmptyStringDto {
            @MandatoryField(cases = { @MandatoryFieldCase(when = "true") })
            String name = "";
        }
        EmptyStringDto dto = new EmptyStringDto();
        when(stepContext.getPayload()).thenReturn(dto);

        when(expressionEvaluations.evaluate("name")).thenReturn(evalNameResolver);
        when(evalNameResolver.getValue(dto)).thenReturn(dto.name); // ""

        BehaviorStep<Object> step = steps.correlationStep();
        Validation<Violations, StepContext<Object>> result = step.execute(stepContext);

        assertTrue(result.isInvalid());
        Violations v = result.error().orElseThrow();
        assertTrue(v.stream().anyMatch(gv ->
                "name".equals(gv.getValidationName()) &&
                        gv.getMessages().stream().anyMatch(m -> m.contains("此欄位必須輸入"))));
    }
}
