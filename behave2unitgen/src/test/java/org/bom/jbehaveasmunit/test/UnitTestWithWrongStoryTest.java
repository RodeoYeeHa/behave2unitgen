package org.bom.jbehaveasmunit.test;

import org.bom.jbehaveasmunit.annotations.Story;
import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.MethodSorters;

@RunWith(BlockJUnit4ClassRunner.class)
@FixMethodOrder(MethodSorters.JVM)
@Story(name="/org/bom/stories/gibtesnicht.story")
public class UnitTestWithWrongStoryTest {

}
