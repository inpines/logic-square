package org.dotspace.oofp.support.dto;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import org.dotspace.oofp.support.transform.TransformMapping;

public class GeneralTransformationRequest<P, T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4482172037746128972L;

//	private Class<T> destinationTypeClazz;
	private Function<P, T> destinations;
	
	private P destinationPatameters;
		
	private Object sourceInfo;

	private List<TransformMapping> transformMappers;
	
	public GeneralTransformationRequest(Supplier<T> destinationSupplier, Object sourceInfo, 
			List<TransformMapping> transformMappers) {
		super();
		this.destinations = p -> destinationSupplier.get();
		this.destinationPatameters = null;
		this.sourceInfo = sourceInfo;
		this.setTransformMappers(transformMappers);
	}

	public GeneralTransformationRequest(Function<P, T> destinations, P destinationParameters,
			Object sourceInfo, 
			List<TransformMapping> transformMappers) {
		super();
		this.destinations = destinations;
		this.destinationPatameters = destinationParameters;
		this.sourceInfo = sourceInfo;
		this.setTransformMappers(transformMappers);
		
	}
	
//	public Class<T> getDestinationTypeClazz() {
//		return destinationTypeClazz;
//	}
//
//	public void setDestinationTypeClazz(Class<T> destinationTypeClazz) {
//		this.destinationTypeClazz = destinationTypeClazz;
//	}

	public Object getSourceInfo() {
		return sourceInfo;
	}

	public void setSourceInfo(Object sourceInfo) {
		this.sourceInfo = sourceInfo;
	}

	public List<TransformMapping> getTransformMappers() {
		return transformMappers;
	}

	public void setTransformMappers(List<TransformMapping> transformMappers) {
		this.transformMappers = transformMappers;
	}

	public Function<P, T> getDestinations() {
		return destinations;
	}

	public P getDestinationPatameters() {
		return destinationPatameters;
	}

}
