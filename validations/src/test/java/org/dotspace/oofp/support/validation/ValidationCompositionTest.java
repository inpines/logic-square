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

public class ValidationCompositionTest {

    private static List<String> validItemNames = Arrays.asList(
            "item name a", 
            "item name b", 
            "item name c");

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

        ValidationComposition<MyModel> myValidating = ValidationCompositions.<MyModel>composing()
            .with(SingularMemberValidationPolicy.select(MyModel::getName)
                .filter(this::validate1)
                .orElse(this::writeViolation1)
                .dontInterruptOnFail())
            .with(PluralMemberValidationPolicy.each(MyModel::getItems)
                .filter(this::validate2)
                .orElse(this::writeViolation2));

        List<GeneralViolation> violations = myValidating
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

        violations = myValidating.validate(model);

        assertNotNull(violations);
        assertTrue(violations.size() == 3);
        assertEquals(violations.get(0).getValidationName(), "validation1");
        assertEquals(violations.get(1).getValidationName(), "validation2.1");
        assertEquals(violations.get(2).getValidationName(), "validation2.2");
    }

    private boolean validate1(
        String name) {

        boolean validated = Optional.ofNullable(name)
        .filter("my name"::equals)
        .map(nm -> true)
        .orElse(false);

        return validated;
    }

    private void writeViolation1(String name, ValidationContext<MyModel> ctx) {
    	ctx.add(GeneralBuilders.of(GeneralViolation::new)
                .with(GeneralBuildingWriters.set(
                    GeneralViolation::setValidationName, "validation1"))
                .with(GeneralBuildingWriters.set(GeneralViolation::setMessages, 
                		Arrays.asList("name is not equals 'my name'")))
                .build());
    }
    
    private boolean validate2(
        String itemName) {

        boolean validated = validItemNames.stream().anyMatch(
            i -> i.equals(itemName));

        return validated;
    }
 
    private void writeViolation2(
            String itemName, ValidationContext<MyModel> ctx) {
        
    	long n = ctx.getViolationsCount("validation2\\.\\d+$") + 1;
    	
        ctx.add(GeneralBuilders.of(GeneralViolation::new)
        .with(GeneralBuildingWriters.set(
            GeneralViolation::setValidationName, String.format("validation2.%d", n)))
        .with(GeneralBuildingWriters.set(
            GeneralViolation::setMessages, Arrays.asList(String.format(
                    "item names is %s not in %s", itemName, 
                    validItemNames.toString()))))
        .build());
    }
}