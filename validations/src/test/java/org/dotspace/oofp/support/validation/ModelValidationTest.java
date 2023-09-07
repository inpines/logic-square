package org.dotspace.oofp.support.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.dotspace.oofp.support.builder.GeneralBuilders;
import org.dotspace.oofp.support.builder.writer.GeneralBuildingWriters;
import org.dotspace.oofp.support.validation.policy.PluralMemberValidationPolicy;
import org.dotspace.oofp.support.validation.policy.SingularMemberValidationPolicy;
import org.dotspace.oofp.util.functional.Casters;
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

        MyModel model = GeneralBuilders.of(MyModel::new)
        		.with(GeneralBuildingWriters.set(MyModel::setName, "my name"))
        		.with(GeneralBuildingWriters.setForEach(MyModel::getItems, 
        				Casters.cast(String.class),
        				Arrays.asList("item name a", "item name c")))
            .build();

        ValidationBuilder<MyModel> myValidator = GenenalValidations.<MyModel>compose()
            .adopt(SingularMemberValidationPolicy.select(MyModel::getName)
                .with(this::validate1)
                .dontInterruptOnFail())
            .adopt(PluralMemberValidationPolicy.each(MyModel::getItems)
                .with(this::validate2));

        List<ModelViolation> violations = myValidator
            .validate(model);
          
        assertNotNull(violations);
        assertTrue(violations.isEmpty());
        
        model = GeneralBuilders.of(MyModel::new)
            .with(GeneralBuildingWriters.set(
                MyModel::setName,"your name"))
            .with(GeneralBuildingWriters.setForEach(
                MyModel::getItems, Casters.cast(String.class), 
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
            ctx.add(GeneralBuilders.of(ModelViolation::new)
            .with(GeneralBuildingWriters.set(
                ModelViolation::setValidationName, "validation1"))
            .with(GeneralBuildingWriters.set(ModelViolation::setMessages, 
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
            ctx.add(GeneralBuilders.of(ModelViolation::new)
            .with(GeneralBuildingWriters.set(
                ModelViolation::setValidationName, "validation2"))
            .with(GeneralBuildingWriters.set(
                ModelViolation::setMessages, Arrays.asList(String.format(
                        "item names is %s not in %s", itemName, 
                        validItemNames.toString()))))
            .build());
        }

        return validated;
    }
    
}