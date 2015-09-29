package burlap.testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestTesting.class,
	TestGridWorld.class,
	TestPlanning.class,
	TestBlockDude.class,
	TestImmutableState.class,
	TestHashing.class
})
public class TestSuite {

}
