package uk.ac.ebi.interpro.metagenomics.memi.springmvc.model;

import uk.ac.ebi.interpro.metagenomics.memi.core.MemiPropertyContainer;
import uk.ac.ebi.interpro.metagenomics.memi.forms.EBISearchForm;
import uk.ac.ebi.interpro.metagenomics.memi.forms.SubmissionForm;
import uk.ac.ebi.interpro.metagenomics.memi.model.apro.Submitter;

import java.util.List;

/**
 * Represents the model for the submission page.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @since 1.0-SNAPSHOT
 */
public class SubmissionModel extends ViewModel {

    private SubmissionForm subForm;

    public SubmissionModel(Submitter submitter, EBISearchForm ebiSearchForm, String pageTitle, List<Breadcrumb> breadcrumbs,
                           MemiPropertyContainer propertyContainer) {
        super(submitter, ebiSearchForm, pageTitle, breadcrumbs, propertyContainer);
        this.subForm = new SubmissionForm();
    }

    public SubmissionForm getSubForm() {
        return subForm;
    }

    public void setSubForm(SubmissionForm subForm) {
        this.subForm = subForm;
    }
}
