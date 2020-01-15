package net.nahknarmi.arch.e2e;

import net.nahknarmi.arch.commands.ValidateCommand;
import org.junit.Test;

import java.io.File;

import static net.nahknarmi.arch.TestHelper.TEST_PRODUCT_DOCUMENTATION_ROOT_PATH;
import static net.nahknarmi.arch.TestHelper.TEST_VALIDATION_ROOT_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class ValidateCommandE2ETest {

    @Test
    public void validate() throws Exception {
        File file = new File(getClass().getResource(TEST_PRODUCT_DOCUMENTATION_ROOT_PATH).getPath());
        ValidateCommand validateCommand = new ValidateCommand(file, "data-structure.yml");

        Integer statusCode = validateCommand.call();

        assertThat(statusCode, equalTo(0));
    }

    @Test
    public void validate_missing_metadata() throws Exception {
        File file = new File(getClass().getResource(TEST_VALIDATION_ROOT_PATH).getPath());
        ValidateCommand validateCommand = new ValidateCommand(file, "missingMetadata.yml");

        Integer statusCode = validateCommand.call();

        assertThat(statusCode, not(equalTo(0)));
    }
}
