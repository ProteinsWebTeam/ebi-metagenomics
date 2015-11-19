package uk.ac.ebi.interpro.metagenomics.memi.springmvc.modelbuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import uk.ac.ebi.interpro.metagenomics.memi.core.MemiPropertyContainer;
import uk.ac.ebi.interpro.metagenomics.memi.core.comparators.HomePageSamplesComparator;
import uk.ac.ebi.interpro.metagenomics.memi.core.tools.MemiTools;
import uk.ac.ebi.interpro.metagenomics.memi.dao.RunDAO;
import uk.ac.ebi.interpro.metagenomics.memi.dao.erapro.SubmissionContactDAO;
import uk.ac.ebi.interpro.metagenomics.memi.dao.hibernate.BiomeDAO;
import uk.ac.ebi.interpro.metagenomics.memi.dao.hibernate.SampleDAO;
import uk.ac.ebi.interpro.metagenomics.memi.dao.hibernate.StudyDAO;
import uk.ac.ebi.interpro.metagenomics.memi.forms.Biome;
import uk.ac.ebi.interpro.metagenomics.memi.model.apro.Submitter;
import uk.ac.ebi.interpro.metagenomics.memi.model.hibernate.Sample;
import uk.ac.ebi.interpro.metagenomics.memi.model.hibernate.Study;
import uk.ac.ebi.interpro.metagenomics.memi.springmvc.model.Breadcrumb;
import uk.ac.ebi.interpro.metagenomics.memi.springmvc.model.HomePageViewModel;
import uk.ac.ebi.interpro.metagenomics.memi.springmvc.session.SessionManager;

import java.util.*;

/**
 * Model builder class for StudyViewModel. See {@link uk.ac.ebi.interpro.metagenomics.memi.springmvc.modelbuilder.ViewModelBuilder} for more information of how to use.
 *
 * @author Maxim Scheremetjew, EMBL-EBI, InterPro
 * @version $Id$
 * @since 1.0-SNAPSHOT
 */
public class HomePageViewModelBuilder extends AbstractBiomeViewModelBuilder<HomePageViewModel> {

    private final static Log log = LogFactory.getLog(HomePageViewModelBuilder.class);

    private String pageTitle;

    private List<Breadcrumb> breadcrumbs;

    private MemiPropertyContainer propertyContainer;

    private StudyDAO studyDAO;

    private SampleDAO sampleDAO;

    private BiomeDAO biomeDAO;

    private RunDAO runDAO;

    private SubmissionContactDAO submissionContactDAO;

    /**
     * The number of latest projects to show on the home page. Used within this builder class, but also within the Java Server Page.
     */
    private final int maxRowNumberOfLatestItems = 15;


    public HomePageViewModelBuilder(SessionManager sessionMgr, String pageTitle, List<Breadcrumb> breadcrumbs, MemiPropertyContainer propertyContainer,
                                    StudyDAO studyDAO, SampleDAO sampleDAO, RunDAO runDAO, BiomeDAO biomeDAO, SubmissionContactDAO submissionContactDAO) {
        super(sessionMgr);
        this.pageTitle = pageTitle;
        this.breadcrumbs = breadcrumbs;
        this.propertyContainer = propertyContainer;
        this.studyDAO = studyDAO;
        this.sampleDAO = sampleDAO;
        this.runDAO = runDAO;
        this.biomeDAO = biomeDAO;
        this.submissionContactDAO = submissionContactDAO;
    }

    public HomePageViewModel getModel() {
        log.info("Building instance of " + HomePageViewModel.class + "...");
        Submitter submitter = getSessionSubmitter(sessionMgr);
        // The following values are all for the statistics section on the home page
        final Long publicSamplesCount = sampleDAO.countAllPublic();
        final Long privateSamplesCount = sampleDAO.countAllPrivate();
        final Long publicStudiesCount = studyDAO.countAllPublic();
        final Long privateStudiesCount = studyDAO.countAllWithNotEqualsEx(1);
        final int publicRunCount = runDAO.countAllPublic();
        final int privateRunCount = runDAO.countAllPrivate();

        final Map<String, Integer> experimentCountMap = runDAO.retrieveRunCountsGroupedByExperimentType(3);
        final Map<String, Integer> transformedExperimentCountMap = transformMap(experimentCountMap);
        final Integer numOfDataSets = getNumOfDataSets(experimentCountMap);

        List<Study> studies = null;
        // If case: if nobody is logged in
        if (submitter == null) {
            // Retrieve public studies limited by max result and order them by last meta data received
            studies = getOrderedPublicStudies();
        }
        //  Else case: if somebody is logged in
        else {
            final String submitterAccountId = submitter.getSubmissionAccountId();
            //retrieve user studies and order them by last meta data received
            studies = retrieveOrderedStudiesBySubmitter(submitterAccountId);
        }

        // Get list of study identifiers
        List<String> studyIdentifiers = getListOfStudyIdentifiers(studies);
        Map<String, Long> studyToSampleCountMap = new HashMap<String, Long>(0);
        Map<String, Long> studyToRunCountMap = new HashMap<String, Long>(0);
        if (studyIdentifiers.size() > 0) {
            // Get study to sample count map
            studyToSampleCountMap = studyDAO.retrieveSampleCountsGroupedByExternalStudyId(studyIdentifiers);
            // Get study to run count map
            studyToRunCountMap = studyDAO.retrieveRunCountsGroupedByExternalStudyId(studyIdentifiers);
        }

        // If case: if nobody is logged in
        if (submitter == null) {
            Map<String, Long> biomeCountMap = buildBiomeCountMap();
            return new HomePageViewModel(submitter, pageTitle, breadcrumbs, propertyContainer, maxRowNumberOfLatestItems, publicSamplesCount,
                    privateSamplesCount, publicStudiesCount, privateStudiesCount, studies, publicRunCount, privateRunCount, biomeCountMap, transformedExperimentCountMap, numOfDataSets,
                    studyToSampleCountMap, studyToRunCountMap);
        }
        //  Else case: if somebody is logged in
        else {
            final String submitterAccountId = submitter.getSubmissionAccountId();
            //Retrieve submitter details for the private area section
            Submitter submitterDetails = submissionContactDAO.getSubmitterBySubmissionAccountId(submitterAccountId);

//            Map<Study, Long> myStudiesMap = getStudySampleSizeMap(myStudies, sampleDAO, new HomePageStudiesComparator());

            //retrieve private samples and order them last meta data received
            List<Sample> mySamples = getSamplesBySubmitter(submitter.getSubmissionAccountId(), sampleDAO);
            Collections.sort(mySamples, new HomePageSamplesComparator());
            mySamples = mySamples.subList(0, getToIndex(mySamples));

            final Long mySamplesCount = (mySamples != null ? new Long(mySamples.size()) : new Long(0));
//            final Long myStudiesCount = (myStudies != null ? new Long(myStudies.size()) : new Long(0));

            return new HomePageViewModel(submitterDetails, studies, mySamples, pageTitle, breadcrumbs, propertyContainer, maxRowNumberOfLatestItems,
                    mySamplesCount, new Long(studies.size()), publicSamplesCount, privateSamplesCount, publicStudiesCount, privateStudiesCount, publicRunCount, privateRunCount,
                    studyToSampleCountMap, studyToRunCountMap);
        }
    }

    private Map<String, Integer> transformMap(Map<String, Integer> experimentCountMap) {
        Map<String, Integer> result = new TreeMap<String, Integer>(
                //Comparator is tested in HomePageSamplesComparatorTest
                new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (o1.equalsIgnoreCase("assemblies") || o2.equalsIgnoreCase("assemblies")) {
                            return 1;
                        } else if (o1.equalsIgnoreCase("metagenomics") || o2.equalsIgnoreCase("metagenomics")) {
                            return -1;
                        } else if (o1.equalsIgnoreCase("amplicons") || o2.equalsIgnoreCase("metatranscriptomics")) {
                            return 1;
                        } else if (o1.equalsIgnoreCase("metatranscriptomics") || o2.equalsIgnoreCase("amplicons")) {
                            return -1;
                        } else {
                            return 0;
                        }
                    }

                });
        for (String key : experimentCountMap.keySet()) {
            Integer value = experimentCountMap.get(key);
            if (key.equalsIgnoreCase("assembly")) {
                result.put("assemblies", value);
            } else {
                result.put(key + 's', value);
            }
        }
        return result;
    }

    private Integer getNumOfDataSets(Map<String, Integer> experimentCountMap) {
        Integer result = 0;
        for (String key : experimentCountMap.keySet()) {
            result += experimentCountMap.get(key);
        }
        return result;
    }

    private Map<String, Long> buildBiomeCountMap() {
        final Map<String, Long> biomesCountMap = new HashMap<String, Long>();
        //Add number of soil biomes
        long studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.SOIL.getLineages());
        biomesCountMap.put(Biome.SOIL.toString(), studyCount);
        //Add number of marine biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.MARINE.getLineages());
        biomesCountMap.put(Biome.MARINE.toString(), studyCount);
        //Add number of forest biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.FOREST_SOIL.getLineages());
        biomesCountMap.put(Biome.FOREST_SOIL.toString(), studyCount);
        //Add number of freshwater biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.FRESHWATER.getLineages());
        biomesCountMap.put(Biome.FRESHWATER.toString(), studyCount);
        //Add number of grassland biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.GRASSLAND.getLineages());
        biomesCountMap.put(Biome.GRASSLAND.toString(), studyCount);
        //Add number of human gut biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.HUMAN_GUT.getLineages());
        biomesCountMap.put(Biome.HUMAN_GUT.toString(), studyCount);
        //Add number of human host biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.HUMAN_HOST.getLineages());
        biomesCountMap.put(Biome.HUMAN_HOST.toString(), studyCount);
        //Add number of non-human host biomes
        long studyCountForHuman = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.HUMAN_HOST.getLineages());
        long studyCountForHost = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.HOST_ASSOCIATED.getLineages());
        long studyCountForNonHumanHost = studyCountForHost - studyCountForHuman;
        biomesCountMap.put(Biome.NON_HUMAN_HOST.toString(), studyCountForNonHumanHost);
        //Add number of engineered biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.ENGINEERED.getLineages());
        biomesCountMap.put(Biome.ENGINEERED.toString(), studyCount);
        //Add number of air biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.AIR.getLineages());
        biomesCountMap.put(Biome.AIR.toString(), studyCount);
        //Add number of wastewater biomes
        studyCount = super.countStudiesFilteredByBiomes(studyDAO, biomeDAO, Biome.WASTEWATER.getLineages());
        biomesCountMap.put(Biome.WASTEWATER.toString(), studyCount);
        return biomesCountMap;
    }

    private List<String> getListOfStudyIdentifiers(final List<Study> studies) {
        List<String> studyIds = new ArrayList<String>();
        for (Study study : studies) {
            studyIds.add(study.getStudyId());
        }
        return studyIds;
    }

    /**
     * Returns a list of public studies limited by a specified number of rows and order by meta data received date.
     */
    private List<Study> getOrderedPublicStudies() {
        List<Study> studies = new ArrayList<Study>();
        if (studyDAO != null) {
            studies = studyDAO.retrieveOrderedPublicStudies("lastMetadataReceived", true, maxRowNumberOfLatestItems);
            assignBiomeIconFeatures(studies);
        }
        return studies;
    }

    private Map<Study, Long> getStudySampleSizeMap(List<Study> studies, SampleDAO sampleDAO, Comparator<Study> comparator) {
        Map<Study, Long> result = new TreeMap<Study, Long>(comparator);
        for (Study study : studies) {
            if (sampleDAO != null) {
                result.put(study, sampleDAO.retrieveSampleSizeByStudyId(study.getId()));
            }
        }
        return result;
    }

    private int getToIndex(Collection<Sample> collection) {
        return ((collection.size() > 5) ? 5 : collection.size());
    }

    protected void assignBiomeIconFeatures(final List<Study> studies) {
        if (studies != null) {
            for (Study study : studies) {
                MemiTools.assignBiomeIconCSSClass(study, biomeDAO);
                MemiTools.assignBiomeIconTitle(study, biomeDAO);
            }
        }
    }

    /**
     * Returns a list of studies for the specified submitter.
     */
    private List<Study> retrieveOrderedStudiesBySubmitter(String submissionAccountId) {
        List<Study> studies = new ArrayList<Study>();
        if (studyDAO != null) {
            studies = studyDAO.retrieveOrderedStudiesBySubmitter(submissionAccountId, "lastMetadataReceived", true, maxRowNumberOfLatestItems);
            assignBiomeIconFeatures(studies);
        }
        return studies;
    }

    /**
     * Returns a list of studies for the specified submitter, limited by a specified number of rows.
     */
    private List<Sample> getSamplesBySubmitter(String submissionAccountId, SampleDAO sampleDAO) {
        List<Sample> samples = new ArrayList<Sample>();
        if (sampleDAO != null) {
            samples = sampleDAO.retrieveSamplesBySubmitter(submissionAccountId);
        }
        return samples;
    }
}