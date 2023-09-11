package org.dotspace.oofp.support.test.functional;

import java.util.function.Function;

import org.dotspace.oofp.support.test.dto.TestReportModel;
import org.dotspace.oofp.util.functional.GeneralFunctors;

public class TestFunctors extends GeneralFunctors {

	public Function<TestReportModel, Amount> readAmount() {
		return x -> new Amount(x.getAmt(), x.getCcy());
	}
}
