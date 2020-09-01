package net.trilogy.arch.generator;

import com.structurizr.Workspace;
import com.structurizr.model.Model;
import com.structurizr.model.Person;
import com.structurizr.model.Relationship;
import com.structurizr.model.SoftwareSystem;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class FunctionalIdGeneratorTest {
    @Test
    public void shouldReturnIDFromAnonymousFunctionForElements() {
        final FunctionalIdGenerator generator = new FunctionalIdGenerator();
        generator.setNext("5");
        assertThat(generator.generateId(newPerson()), is(equalTo("5")));
    }

    @Test
    public void shouldReturnIDFromAnonymousFunctionForRelationships() {
        final FunctionalIdGenerator generator = new FunctionalIdGenerator();
        generator.setNext("300");
        assertThat(generator.generateId(newRelationship()), is(equalTo("300")));
    }

    @Test(expected = FunctionalIdGenerator.NoNextIdSetException.class)
    public void shouldNotGenerateIDUnlessSet() {
        final FunctionalIdGenerator generator = new FunctionalIdGenerator();
        generator.generateId(newRelationship());
    }

    @Test(expected = FunctionalIdGenerator.NoNextIdSetException.class)
    public void shouldNotGenerateIDTwiceIfSetOnce() {
        final FunctionalIdGenerator generator = new FunctionalIdGenerator();
        generator.setNext("300");
        generator.generateId(newPerson());
        generator.generateId(newPerson());
    }

    @Test
    public void shouldUseDefaultGeneratorIfSetForRelationships() {
        final FunctionalIdGenerator generator = new FunctionalIdGenerator();
        generator.setNext("-");
        generator.setNext("Next");
        generator.setDefaultForRelationships((r) -> "Default");
        assertThat(generator.generateId(newRelationship()), is(equalTo("Next")));
        assertThat(generator.generateId(newRelationship()), is(equalTo("Default")));
        assertThat(generator.generateId(newRelationship()), is(equalTo("Default")));
        generator.setNext("Next");
        assertThat(generator.generateId(newRelationship()), is(equalTo("Next")));
        assertThat(generator.generateId(newRelationship()), is(equalTo("Default")));
    }

    @Test(expected = FunctionalIdGenerator.NoNextIdSetException.class)
    public void shouldClearDefaultGeneratorForRelationships() {
        final FunctionalIdGenerator generator = new FunctionalIdGenerator();

        generator.setDefaultForRelationships((r) -> "Default");
        assertThat(generator.generateId(newRelationship()), is(equalTo("Default")));

        generator.setNext("Next");
        generator.clearDefaultForRelationships();

        assertThat(generator.generateId(newRelationship()), is(equalTo("Next")));
        generator.generateId(newRelationship());
    }

    @Test(expected = FunctionalIdGenerator.NoNextIdSetException.class)
    public void shouldNotUseDefaultGeneratorForRelationshipsForElements() {
        final FunctionalIdGenerator generator = new FunctionalIdGenerator();

        generator.setDefaultForRelationships((r) -> "Default");
        assertThat(generator.generateId(newRelationship()), is(equalTo("Default")));

        generator.setNext("Next");
        assertThat(generator.generateId(newPerson()), is(equalTo("Next")));

        generator.generateId(newPerson());
    }

    private Person newPerson() {
        Workspace workspace = new Workspace("foo", "blah");
        return workspace.getModel().addPerson("abc", "def");
    }

    private Relationship newRelationship() {
        Workspace workspace = new Workspace("foo", "blah");
        Model model = workspace.getModel();
        Person person = model.addPerson("abc", "def");
        SoftwareSystem system = model.addSoftwareSystem("def", "ghi");
        return person.uses(system, "jkl");
    }
}
