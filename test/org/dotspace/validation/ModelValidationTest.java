package org.dotspace.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.dotspace.creation.Creations;
import org.dotspace.creation.expression.PluralMemberPath;
import org.dotspace.creation.expression.SingularPath;
import org.dotspace.creation.functional.Casters;
import org.dotspace.validation.policy.PluralMemberValidationPolicy;
import org.dotspace.validation.policy.SingularMemberValidationPolicy;
import org.junit.Test;

public class ModelValidationTest {

    public class MyModel {

        private String name;

        private List<String> items = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
        
        public List<String> getItems() {
            return items;
        }

    } 

    @Test
    public void testValidation() {

        MyModel model = Creations.construct(MyModel::new)
            .take(SingularPath.getRootToSet(
                MyModel::setName).assign("my name"))
            .take(PluralMemberPath.getToSet(
                MyModel::getItems, Casters.cast(String.class)).assign(
                Arrays.asList("item name a", "item name c")
            ))
            .build();

        ValidationBuilder<MyModel> myValidator = ModelValidations.typeOf(
            MyModel.class)
            .adopt(SingularMemberValidationPolicy.select(MyModel::getName)
                .with(this::validate1)
                .dontInterruptOnFail())
            .adopt(PluralMemberValidationPolicy.each(MyModel::getItems)
                .with(this::validate2));

        List<ModelViolation> violations = myValidator
            .validate(model);
          
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        
        model = Creations.construct(MyModel::new)
            .take(SingularPath.getRootToSet(
                MyModel::setName).assign("your name"))
            .take(PluralMemberPath.getToSet(
                MyModel::getItems, Casters.cast(String.class)).assign(
                Arrays.asList("item name a", "item name d")
            ))
            .build();

        violations = myValidator.validate(model);

        assertNotNull(violations);
        assertTrue(violations.size() == 2);
        assertEquals(violations.get(0).getValidationName(), "validation1");
        assertEquals(violations.get(1).getValidationName(), "validation2");
    }

    private boolean validate1(
        String name, ValidationsContext<MyModel> ctx) {

        boolean validated = Optional.ofNullable(name)
        .filter("my name"::equals)
        .map(nm -> true)
        .orElseGet(() -> {
            ctx.add(Creations.construct(ModelViolation::new)
            .take(SingularPath.getRootToSet(
                ModelViolation::setValidationName).assign(
                    "validation1"))
            .take(SingularPath.getRootToSet(
                ModelViolation::setMessages).assign(
                    Arrays.asList("name is not equals 'my name'")))
            .build());
            return false;
        });

        return validated;
    }

    private boolean validate2(
        String itemName, ValidationsContext<MyModel> ctx) {

        List<String> validItemNames = Arrays.asList(
            "item name a", 
            "item name b", 
            "item name c");

        boolean validated = validItemNames.stream().anyMatch(
            i -> i.equals(itemName));

        if (!validated) {
            ctx.add(Creations.construct(ModelViolation::new)
            .take(SingularPath.getRootToSet(
                ModelViolation::setValidationName).assign(
                    "validation2"))
            .take(SingularPath.getRootToSet(
                ModelViolation::setMessages).assign(
                    Arrays.asList(String.format(
                        "item names is %s not in %s", itemName, 
                        validItemNames.toString()))))
            .build());
        }

        return validated;
    }
    
}