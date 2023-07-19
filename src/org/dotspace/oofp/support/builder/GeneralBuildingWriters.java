package org.dotspace.oofp.support.builder;

public interface GeneralBuildingWriters<T, D> {

	public GeneralBuildingWriter<T, D> write(D data);
	
}
