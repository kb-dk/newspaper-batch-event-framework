package dk.statsbiblioteket.medieplatform.autonomous;

import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class SolrProxyIteratorTest {

    @BeforeMethod
    public void setUp() throws Exception {

    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test(groups = "externalTest", enabled = false)
    /**
     * This test tests the situation where were long querys generated 414 errors in the production system.
     * It is disabled since it only tests specific behaviour with apache frontend.
     */
    public void testNext() throws Exception {
        String longstring = "recordBase:doms_sboiCollection\n" + "   (  ( +newspapr_batch_id:B400027101435 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131490 )\n" + "  OR  ( +newspapr_batch_id:B400026952075 )\n"
                + "  OR  ( +newspapr_batch_id:B400027043931 )\n" + "  OR  ( +newspapr_batch_id:B400026951974 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954949 )\n" + "  OR  ( +newspapr_batch_id:B400026951885 )\n"
                + "  OR  ( +newspapr_batch_id:B400026103434 )\n" + "  OR  ( +newspapr_batch_id:B400027131644 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954531 )\n" + "  OR  ( +newspapr_batch_id:B400026954426 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955546 )\n" + "  OR  ( +newspapr_batch_id:B400026954922 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952121 )\n" + "  OR  ( +newspapr_batch_id:B400026954582 )\n"
                + "  OR  ( +newspapr_batch_id:B400026959487 )\n" + "  OR  ( +newspapr_batch_id:B400027131504 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955902 )\n" + "  OR  ( +newspapr_batch_id:B400026955872 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955228 )\n" + "  OR  ( +newspapr_batch_id:B400027131903 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951915 )\n" + "  OR  ( +newspapr_batch_id:B400026955430 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955716 )\n" + "  OR  ( +newspapr_batch_id:B400026954523 )\n"
                + "  OR  ( +newspapr_batch_id:B400027043885 )\n" + "  OR  ( +newspapr_batch_id:B400026955767 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952024 )\n" + "  OR  ( +newspapr_batch_id:B400026954477 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131601 )\n" + "  OR  ( +newspapr_batch_id:B400026955384 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954841 )\n" + "  OR  ( +newspapr_batch_id:B400027131891 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951818 )\n" + "  OR  ( +newspapr_batch_id:B400027131881 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954787 )\n" + "  OR  ( +newspapr_batch_id:B400026955813 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954388 )\n" + "  OR  ( +newspapr_batch_id:B400026959551 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952148 )\n" + "  OR  ( +newspapr_batch_id:B400026103620 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951771 )\n" + "  OR  ( +newspapr_batch_id:B400026955325 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955279 )\n" + "  OR  ( +newspapr_batch_id:B400026954892 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954728 )\n" + "  OR  ( +newspapr_batch_id:B400027131717 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955481 )\n" + "  OR  ( +newspapr_batch_id:B400027043818 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951923 )\n" + "  OR  ( +newspapr_batch_id:B400026954361 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954371 )\n" + "  OR  ( +newspapr_batch_id:B400026955376 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955708 )\n" + "  OR  ( +newspapr_batch_id:B400026959525 )\n"
                + "  OR  ( +newspapr_batch_id:B400026959381 )\n" + "  OR  ( +newspapr_batch_id:B400026955821 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954418 )\n" + "  OR  ( +newspapr_batch_id:B400026959495 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955880 )\n" + "  OR  ( +newspapr_batch_id:B400026954825 )\n"
                + "  OR  ( +newspapr_batch_id:B400026959576 )\n" + "  OR  ( +newspapr_batch_id:B400026955831 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955732 )\n" + "  OR  ( +newspapr_batch_id:B400026955392 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951850 )\n" + "  OR  ( +newspapr_batch_id:B400026954469 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955503 )\n" + "  OR  ( +newspapr_batch_id:B400026951826 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954711 )\n" + "  OR  ( +newspapr_batch_id:B400027131598 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955805 )\n" + "  OR  ( +newspapr_batch_id:B400026951788 )\n"
                + "  OR  ( +newspapr_batch_id:B400026959371 )\n" + "  OR  ( +newspapr_batch_id:B400026955351 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955783 )\n" + "  OR  ( +newspapr_batch_id:B400026955317 )\n"
                + "  OR  ( +newspapr_batch_id:B400026959509 )\n" + "  OR  ( +newspapr_batch_id:B400026959444 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955341 )\n" + "  OR  ( +newspapr_batch_id:B400026954396 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951796 )\n" + "  OR  ( +newspapr_batch_id:B400026955368 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954401 )\n" + "  OR  ( +newspapr_batch_id:B400026954612 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131628 )\n" + "  OR  ( +newspapr_batch_id:B400027131547 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954779 )\n" + "  OR  ( +newspapr_batch_id:B400026954442 )\n"
                + "  OR  ( +newspapr_batch_id:B400026103361 )\n" + "  OR  ( +newspapr_batch_id:B400027043834 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131784 )\n" + "  OR  ( +newspapr_batch_id:B400026955759 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954515 )\n" + "  OR  ( +newspapr_batch_id:B400026952083 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954833 )\n" + "  OR  ( +newspapr_batch_id:B400026954353 )\n"
                + "  OR  ( +newspapr_batch_id:B400026103604 )\n" + "  OR  ( +newspapr_batch_id:B400026955465 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131997 )\n" + "  OR  ( +newspapr_batch_id:B400027131636 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951893 )\n" + "  OR  ( +newspapr_batch_id:B400027131751 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955406 )\n" + "  OR  ( +newspapr_batch_id:B400027131741 )\n"
                + "  OR  ( +newspapr_batch_id:B400027043801 )\n" + "  OR  ( +newspapr_batch_id:B400026955929 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955511 )\n" + "  OR  ( +newspapr_batch_id:B400027043842 )\n"
                + "  OR  ( +newspapr_batch_id:B400027101494 )\n" + "  OR  ( +newspapr_batch_id:B400026954590 )\n"
                + "  OR  ( +newspapr_batch_id:B400027101281 )\n" + "  OR  ( +newspapr_batch_id:B400027131792 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955521 )\n" + "  OR  ( +newspapr_batch_id:B400027131695 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131581 )\n" + "  OR  ( +newspapr_batch_id:B400026954752 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955295 )\n" + "  OR  ( +newspapr_batch_id:B400027043893 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954876 )\n" + "  OR  ( +newspapr_batch_id:B400026952032 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955491 )\n" + "  OR  ( +newspapr_batch_id:B400027131962 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131539 )\n" + "  OR  ( +newspapr_batch_id:B400026954663 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131776 )\n" + "  OR  ( +newspapr_batch_id:B400027131989 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955848 )\n" + "  OR  ( +newspapr_batch_id:B400026955899 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954450 )\n" + "  OR  ( +newspapr_batch_id:B400027043788 )\n"
                + "  OR  ( +newspapr_batch_id:B400027101486 )\n" + "  OR  ( +newspapr_batch_id:B400026959401 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952156 )\n" + "  OR  ( +newspapr_batch_id:B400026103401 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955953 )\n" + "  OR  ( +newspapr_batch_id:B400026959517 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955211 )\n" + "  OR  ( +newspapr_batch_id:B400026954760 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955570 )\n" + "  OR  ( +newspapr_batch_id:B400027132012 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955791 )\n" + "  OR  ( +newspapr_batch_id:B400027101508 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951877 )\n" + "  OR  ( +newspapr_batch_id:B400026951753 )\n"
                + "  OR  ( +newspapr_batch_id:B400027043850 )\n" + "  OR  ( +newspapr_batch_id:B400026954868 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955686 )\n" + "  OR  ( +newspapr_batch_id:B400026955414 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954558 )\n" + "  OR  ( +newspapr_batch_id:B400026954817 )\n"
                + "  OR  ( +newspapr_batch_id:B400026103590 )\n" + "  OR  ( +newspapr_batch_id:B400026951834 )\n"
                + "  OR  ( +newspapr_batch_id:B400027101613 )\n" + "  OR  ( +newspapr_batch_id:B400027043753 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131611 )\n" + "  OR  ( +newspapr_batch_id:B400027131849 )\n"
                + "  OR  ( +newspapr_batch_id:B400027043826 )\n" + "  OR  ( +newspapr_batch_id:B400026952040 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954310 )\n" + "  OR  ( +newspapr_batch_id:B400026954493 )\n"
                + "  OR  ( +newspapr_batch_id:B400027043796 )\n" + "  OR  ( +newspapr_batch_id:B400026955244 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954681 )\n" + "  OR  ( +newspapr_batch_id:B400027101583 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951801 )\n" + "  OR  ( +newspapr_batch_id:B400027131725 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955740 )\n" + "  OR  ( +newspapr_batch_id:B400026954957 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955597 )\n" + "  OR  ( +newspapr_batch_id:B400027131921 )\n"
                + "  OR  ( +newspapr_batch_id:B400027101273 )\n" + "  OR  ( +newspapr_batch_id:B400026955661 )\n"
                + "  OR  ( +newspapr_batch_id:B400027101621 )\n" + "  OR  ( +newspapr_batch_id:B400026954604 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952131 )\n" + "  OR  ( +newspapr_batch_id:B400027043923 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952091 )\n" + "  OR  ( +newspapr_batch_id:B400026951842 )\n"
                + "  OR  ( +newspapr_batch_id:B400027043877 )\n" + "  OR  ( +newspapr_batch_id:B400026955457 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131822 )\n" + "  OR  ( +newspapr_batch_id:B400026952016 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954345 )\n" + "  OR  ( +newspapr_batch_id:B400027101516 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955252 )\n" + "  OR  ( +newspapr_batch_id:B400026952008 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955287 )\n" + "  OR  ( +newspapr_batch_id:B400027043761 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131709 )\n" + "  OR  ( +newspapr_batch_id:B400026954884 )\n"
                + "  OR  ( +newspapr_batch_id:B400027101605 )\n" + "  OR  ( +newspapr_batch_id:B400026955678 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955236 )\n" + "  OR  ( +newspapr_batch_id:B400026954574 )\n"
                + "  OR  ( +newspapr_batch_id:B400027132055 )\n" + "  OR  ( +newspapr_batch_id:B400027131679 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954906 )\n" + "  OR  ( +newspapr_batch_id:B400026952105 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954647 )\n" + "  OR  ( +newspapr_batch_id:B400027131652 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955937 )\n" + "  OR  ( +newspapr_batch_id:B400026954507 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954965 )\n" + "  OR  ( +newspapr_batch_id:B400027043915 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955724 )\n" + "  OR  ( +newspapr_batch_id:B400027131814 )\n"
                + "  OR  ( +newspapr_batch_id:B400026959533 )\n" + "  OR  ( +newspapr_batch_id:B400027043869 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952067 )\n" + "  OR  ( +newspapr_batch_id:B400026951982 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955260 )\n" + "  OR  ( +newspapr_batch_id:B400026954337 )\n"
                + "  OR  ( +newspapr_batch_id:B400026103493 )\n" + "  OR  ( +newspapr_batch_id:B400027131563 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952113 )\n" + "  OR  ( +newspapr_batch_id:B400026954434 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954914 )\n" + "  OR  ( +newspapr_batch_id:B400026954671 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131768 )\n" + "  OR  ( +newspapr_batch_id:B400027043907 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951941 )\n" + "  OR  ( +newspapr_batch_id:B400027101311 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954620 )\n" + "  OR  ( +newspapr_batch_id:B400026103442 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951931 )\n" + "  OR  ( +newspapr_batch_id:B400026955333 )\n"
                + "  OR  ( +newspapr_batch_id:B400026952059 )\n" + "  OR  ( +newspapr_batch_id:B400027101631 )\n"
                + "  OR  ( +newspapr_batch_id:B400027043771 )\n" + "  OR  ( +newspapr_batch_id:B400026955910 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955309 )\n" + "  OR  ( +newspapr_batch_id:B400026951907 )\n"
                + "  OR  ( +newspapr_batch_id:B400026955562 )\n" + "  OR  ( +newspapr_batch_id:B400026951869 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951761 )\n" + "  OR  ( +newspapr_batch_id:B400027132004 )\n"
                + "  OR  ( +newspapr_batch_id:B400027131873 )\n" + "  OR  ( +newspapr_batch_id:B400027131660 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954809 )\n" + "  OR  ( +newspapr_batch_id:B400027131806 )\n"
                + "  OR  ( +newspapr_batch_id:B400026951958 )\n" + "  OR  ( +newspapr_batch_id:B400026954485 )\n"
                + "  OR  ( +newspapr_batch_id:B400026103612 )\n" + "  OR  ( +newspapr_batch_id:B400027131512 )\n"
                + "  OR  ( +newspapr_batch_id:B400026954701 )\n" + "  )\n"
                + "  AND  -event:\"Roundtrip_Approved\"  AND  -event:\"Manually_stopped\" ";
        new SolrProxyIterator("item_uuid:*" + longstring, false, new HttpSolrServer("http://prod-search-avis/newspapr/sbsolr/"),new PremisManipulatorFactory("doms:ContentModel_Item", Item::new), new DomsEventStorageFactory().build(), 10);
    }
}