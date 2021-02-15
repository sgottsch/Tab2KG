package de.l3s.simpleml.tab2kg.model.template;

public class TemplateTriple {

	private String subjectPlaceHolder;
	private String predicatePlaceHolder;
	private String objectPlaceHolder;

	public TemplateTriple(String subjectPlaceHolder, String predicatePlaceHolder, String objectPlaceHolder) {
		super();
		this.subjectPlaceHolder = subjectPlaceHolder;
		this.predicatePlaceHolder = predicatePlaceHolder;
		this.objectPlaceHolder = objectPlaceHolder;
	}

	public String getSubjectPlaceHolder() {
		return subjectPlaceHolder;
	}

	public String getPredicatePlaceHolder() {
		return predicatePlaceHolder;
	}

	public String getObjectPlaceHolder() {
		return objectPlaceHolder;
	}

}
