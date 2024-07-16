/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.validator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptReferenceRange;
import org.openmrs.Drug;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.test.jupiter.BaseContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

/**
 * Tests methods on the {@link ObsValidator} class.
 */
public class ObsValidatorTest extends BaseContextSensitiveTest {
	
	@Autowired
	private ObsValidator obsValidator;

	@Mock
	private ConceptService conceptService;

	Calendar calendar = Calendar.getInstance();

	@BeforeEach
	public void setUp() {
		calendar = Calendar.getInstance();
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfPersonIdIsNull() {
		Obs obs = new Obs();
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertTrue(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsDatetimeIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("obsDatetime"));
		assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfParentObshasValues() {
		
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(18));
		
		obs.setValueBoolean(false);
		obs.setValueCoded(Context.getConceptService().getConcept(18));
		obs.setValueComplex("test");
		obs.setValueDatetime(new Date());
		obs.setValueDrug(Context.getConceptService().getDrug(3));
		obs.setValueGroupId(getLoadCount());
		obs.setValueModifier("test");
		obs.setValueNumeric(1212.0);
		obs.setValueText("test");
		
		Set<Obs> group = new HashSet<>();
		group.add(Context.getObsService().getObs(7));
		group.add(Context.getObsService().getObs(9));
		obs.setGroupMembers(group);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertTrue(errors.hasFieldErrors("valueCoded"));
		assertTrue(errors.hasFieldErrors("valueDrug"));
		assertTrue(errors.hasFieldErrors("valueDatetime"));
		assertTrue(errors.hasFieldErrors("valueNumeric"));
		assertTrue(errors.hasFieldErrors("valueModifier"));
		assertTrue(errors.hasFieldErrors("valueText"));
		assertTrue(errors.hasFieldErrors("valueBoolean"));
		assertTrue(errors.hasFieldErrors("valueComplex"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailIfObsHasNoValuesAndNotParent() {
		
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(18));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertTrue(errors.getGlobalErrorCount() > 0);
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertTrue(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertFalse(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsBooleanAndValueBooleanIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(18));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueBoolean"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsCodedAndValueCodedIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(4));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueCoded"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsDateAndValueDatetimeIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(20));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueDatetime"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsNumericAndValueNumericIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueNumeric"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfConceptDatatypeIsTextAndValueTextIsNull() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(19));
		obs.setObsDatetime(new Date());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertTrue(errors.hasFieldErrors("valueText"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsAncestorsContainsObs() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(3)); // datatype = N/A
		obs.setObsDatetime(new Date());
		
		Set<Obs> group = new HashSet<>();
		group.add(obs);
		obs.setGroupMembers(group);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("groupMembers"));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfAllValuesPresent() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfValueTextIsGreaterThanTheMaximumLength() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(19));
		obs.setObsDatetime(new Date());
		
		// Generate 65535+ characters length text.
		StringBuilder valueText = new StringBuilder();
		for (int i = 0; i < 730; i++) {
			valueText
					.append("This text should not exceed 65535 characters. Below code will generate a text more than 65535");
		}
		
		obs.setValueText(valueText.toString());
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertTrue(errors.hasFieldErrors("valueText"));
	}
	
	/**
	 * @see ObsValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldRejectAnInvalidConceptAndDrugCombination() {
		Obs obs = new Obs();
		obs.setPerson(new Person(7));
		obs.setObsDatetime(new Date());
		Concept questionConcept = new Concept(100);
		ConceptDatatype dt = new ConceptDatatype(1);
		dt.setUuid(ConceptDatatype.CODED_UUID);
		questionConcept.setDatatype(dt);
		obs.setConcept(questionConcept);
		obs.setValueCoded(new Concept(101));
		
		Drug drug = new Drug();
		drug.setConcept(new Concept(102));
		obs.setValueDrug(drug);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertTrue(errors.hasFieldErrors("valueDrug"));
	}
	
	/**
	 * @see ObsValidator#validate(Object,Errors)
	 */
	@Test
	public void validate_shouldPassIfAnswerConceptAndConceptOfValueDrugMatch() {
		Obs obs = new Obs();
		obs.setPerson(new Person(7));
		obs.setObsDatetime(new Date());
		Concept questionConcept = new Concept(100);
		ConceptDatatype dt = new ConceptDatatype(1);
		dt.setUuid(ConceptDatatype.CODED_UUID);
		questionConcept.setDatatype(dt);
		obs.setConcept(questionConcept);
		Concept answerConcept = new Concept(101);
		obs.setValueCoded(answerConcept);
		
		Drug drug = new Drug();
		drug.setConcept(answerConcept);
		obs.setValueDrug(drug);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertFalse(errors.hasFieldErrors());
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfFieldLengthsAreCorrect() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		obs.setAccessionNumber("AccessionNumber");
		obs.setValueModifier("m");
		obs.setValueComplex("ValueComplex");
		obs.setVoidReason("VoidReason");
		obs.setComment("comment");
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfFieldLengthsAreNotCorrect() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(1.0);
		
		obs.setAccessionNumber("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		obs.setValueModifier("too long text");
		obs.setValueComplex(StringUtils.repeat("a", 1001));
		obs.setVoidReason("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		obs.setComment("too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text too long text");
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		
		assertTrue(errors.hasFieldErrors("accessionNumber"));
		assertTrue(errors.hasFieldErrors("valueModifier"));
		assertTrue(errors.hasFieldErrors("valueComplex"));
		assertTrue(errors.hasFieldErrors("comment"));
		assertTrue(errors.hasFieldErrors("voidReason"));
	}
	
	/**
	 * @see ObsValidator#supports(Class)
	 */
	@Test
	public void supports_shouldSupportObsClass() {
		assertTrue(obsValidator.supports(Obs.class));
		assertFalse(obsValidator.supports(Concept.class));
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldNotValidateIfObsIsVoided() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		obs.setValueNumeric(null);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertTrue(errors.hasFieldErrors("valueNumeric"));
		
		obs.setVoided(true);
		errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertFalse(errors.hasErrors());
		
	}
	
	/**
	 * @see ObsValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldNotValidateAVoidedChildObs() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(5089));
		obs.setObsDatetime(new Date());
		Obs validChild = new Obs();
		validChild.setPerson(Context.getPersonService().getPerson(2));
		validChild.setConcept(Context.getConceptService().getConcept(5089));
		validChild.setObsDatetime(new Date());
		validChild.setValueNumeric(80.0);
		obs.addGroupMember(validChild);
		Obs inValidChild = new Obs();
		obs.addGroupMember(inValidChild);
		
		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertTrue(errors.hasErrors());
		
		inValidChild.setVoided(true);
		errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);
		assertFalse(errors.hasErrors());
	}
	
	/**
	 * @see ObsValidator#validate(Object, Errors)
	 */
	@Test
	public void validate_shouldFailForANullObject() {
		APIException exception = assertThrows(APIException.class, () -> obsValidator.validate(null, null));
		assertThat(exception.getMessage(), is(CoreMatchers.equalTo("Obs can't be null"))); 
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfValueTextIsLessThanTheMaximumLength() {
		Obs obs = new Obs();
		obs.setPerson(Context.getPersonService().getPerson(2));
		obs.setConcept(Context.getConceptService().getConcept(19));
		obs.setObsDatetime(new Date());

		// Generate 2700+ characters length text.
		StringBuilder valueText = new StringBuilder();
		for (int i = 0; i < 30; i++) {
			valueText
					.append("This text should not exceed 65535 characters. Below code will generate a text Less than 65535");
		}

		obs.setValueText(valueText.toString());

		Errors errors = new BindException(obs, "obs");
		new ObsValidator().validate(obs, errors);

		assertFalse(errors.hasFieldErrors("person"));
		assertFalse(errors.hasFieldErrors("concept"));
		assertFalse(errors.hasFieldErrors("obsDatetime"));
		assertFalse(errors.hasFieldErrors("valueText"));
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsValueExceedsHiAbsolute() {
		Concept concept = new Concept(10);
		concept.setDatatype(new ConceptDatatype(1));

		ConceptReferenceRange conceptReferenceRange = new ConceptReferenceRange();
		conceptReferenceRange.setConcept(concept);
		conceptReferenceRange.setHiAbsolute(150.0);
		conceptReferenceRange.setCriteria("${fn.getAge(1-10)}");
		
		Obs obs = new Obs();
		obs.setId(1);
		obs.setPerson(new Person(10));
		obs.setConcept(concept);
		obs.setValueNumeric(200.0);
		obs.setObsDatetime(new Date());

		List<ConceptReferenceRange> conceptReferenceRanges = Collections.singletonList(conceptReferenceRange);
		when(conceptService.getConceptReferenceRangesByConceptId(anyInt())).thenReturn(conceptReferenceRanges);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertTrue(errors.hasFieldErrors("valueNumeric"));
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsValueBelowLowAbsolute() {
		Obs obs = new Obs();
		obs.setPerson(new Person(10));
		obs.setConcept(getTestConcept());
		obs.setValueNumeric(50.0);
		obs.setObsDatetime(new Date());

		ConceptReferenceRange range = new ConceptReferenceRange();
		range.setLowAbsolute(60.0);
		range.setCriteria("${fn.getAge(1-10)}");

		List<ConceptReferenceRange> conceptReferenceRanges = Collections.singletonList(range);
		when(conceptService.getConceptReferenceRangesByConceptId(anyInt())).thenReturn(conceptReferenceRanges);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertTrue(errors.hasFieldErrors("valueNumeric"));
	}

	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldFailValidationIfObsAgeNotInRange() {
		ConceptReferenceRange conceptReferenceRange = new ConceptReferenceRange();
		conceptReferenceRange.setConcept(getTestConcept());
		conceptReferenceRange.setHiAbsolute(150.0);
		conceptReferenceRange.setCriteria("${fn.getAge(1-10)}");
		
		Person person = new Person();
		calendar.set(2000, Calendar.JANUARY, 1);
		person.setBirthdate(calendar.getTime());
		
		Obs obs = new Obs();
		obs.setId(1);
		obs.setPerson(person);
		obs.setConcept(getTestConcept());
		obs.setValueNumeric(140.0);
		obs.setObsDatetime(new Date());

		List<ConceptReferenceRange> conceptReferenceRanges = Collections.singletonList(conceptReferenceRange);
		when(conceptService.getConceptReferenceRangesByConceptId(anyInt())).thenReturn(conceptReferenceRanges);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertTrue(errors.hasFieldErrors("valueNumeric"));

		assertTrue(errors.hasFieldErrors("valueNumeric"));
		assertNotNull(errors.getFieldError("valueNumeric"));
		assertEquals("error.outOfRange.for.ageGroup", errors.getFieldError("valueNumeric").getCode());
	}
	
	/**
	 * @see ObsValidator#validate(java.lang.Object, org.springframework.validation.Errors)
	 */
	@Test
	public void validate_shouldPassValidationIfObsWithinRange() {
		Obs obs = new Obs();
		Person person = new Person(1);
		calendar.add(Calendar.YEAR, -5);
		
		person.setBirthdate(calendar.getTime());
		obs.setPerson(person);
		obs.setConcept(getTestConcept());
		obs.setValueNumeric(80.0);
		obs.setObsDatetime(new Date());

		ConceptReferenceRange range = new ConceptReferenceRange();
		range.setHiAbsolute(150.0);
		range.setLowAbsolute(60.0);
		range.setCriteria("${fn.getAge(1-10)}");

		List<ConceptReferenceRange> conceptReferenceRanges = Collections.singletonList(range);
		when(conceptService.getConceptReferenceRangesByConceptId(anyInt())).thenReturn(conceptReferenceRanges);

		Errors errors = new BindException(obs, "obs");
		obsValidator.validate(obs, errors);

		assertFalse(errors.hasErrors());
	}
	
	private Concept getTestConcept() {
		Concept concept = new Concept(10);
		concept.setDatatype(new ConceptDatatype(1));
		
		return concept;
	}
}
