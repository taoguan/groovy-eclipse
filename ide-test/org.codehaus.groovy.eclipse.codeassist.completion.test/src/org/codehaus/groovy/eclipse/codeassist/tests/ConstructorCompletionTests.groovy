/*
 * Copyright 2009-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.eclipse.codeassist.tests

import static org.eclipse.jdt.core.tests.util.GroovyUtils.isAtLeastGroovy
import static org.junit.Assume.assumeTrue

import org.codehaus.groovy.eclipse.codeassist.GroovyContentAssist
import org.eclipse.jface.text.contentassist.ICompletionProposal
import org.junit.Before
import org.junit.Test

/**
 * Tests that constructor completions are working properly.  Ensures that the
 * resulting document has the correct text in it.
 */
final class ConstructorCompletionTests extends CompletionTestSuite {

    @Before
    void setUp() {
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.CLOSURE_NOPARENS, false)
        GroovyContentAssist.default.preferenceStore.setValue(GroovyContentAssist.PARAMETER_GUESSING, true)
    }

    @Test
    void testConstructorCompletion1() {
        String contents = 'class YYY { YYY() { } }\nnew YY\nkkk'
        String expected = 'class YYY { YYY() { } }\nnew YYY()\nkkk'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion1a() {
        String contents = 'class YYY { YYY() { } }\nnew YY()\nkkk'
        String expected = 'class YYY { YYY() { } }\nnew YYY()\nkkk'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion2() {
        String contents = 'class YYY { YYY(x) { } }\nnew YY\nkkk'
        String expected = 'class YYY { YYY(x) { } }\nnew YYY(x)\nkkk'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testConstructorCompletion3() {
        String contents = 'class YYY { YYY(x, y) { } }\nnew YY\nkkk'
        String expected = 'class YYY { YYY(x, y) { } }\nnew YYY(x, y)\nkkk'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testContructorCompletionWithinEnumDeclaration1() {
        assumeTrue(isAtLeastGroovy(21))
        String contents = 'class YYY { YYY() { } }\nenum F {\n' +
            '	Aaa() {\n@Override int foo() {\nnew YY\n}\n}\nint foo() {\n	}\n}'
        String expected = 'class YYY { YYY() { } }\nenum F {\n' +
            '	Aaa() {\n@Override int foo() {\nnew YYY()\n}\n}\nint foo() {\n	}\n}'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test
    void testContructorCompletionWithinEnumDeclaration2() {
        String contents = 'class YYY { YYY() { } }\nenum F {\n' +
            '	Aaa {\n@Override int foo() {\nnew YY\n}\n}\nint foo() {\n	}\n}'
        String expected = 'class YYY { YYY() { } }\nenum F {\n' +
            '	Aaa {\n@Override int foo() {\nnew YYY()\n}\n}\nint foo() {\n	}\n}'
        checkProposalApplicationNonType(contents, expected, getIndexOf(contents, 'new YY'), 'YYY')
    }

    @Test // no named args since an explicit constructor exists
    void testNoNamedArgs1() {
        String contents = '''\
            class Flar {
              Flar() { }
              String aaa
              int bbb
              Date ccc
            }
            new Flar()
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'Flar', 1)
    }

    @Test // no named args since an explicit constructor exists
    void testNoNamedArgs2() {
        addGroovySource('''\
            class Flar {
              Flar() { }
              Flar(a,b,c) { }
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar')

        String contents = 'new Flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'Flar', 2)
    }

    @Test // no named args since an explicit constructor exists
    void testNoNamedArgs3() {
        addGroovySource('''\
            class Flar {
              Flar() { }
              Flar(a,b,c) { }
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = 'new Flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'Flar', 2)
    }

    @Test
    void testNamedArgs1() {
        String contents = '''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            new Flar()
            '''.stripIndent()
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'Flar', 1)
    }

    @Test
    void testNamedArgs2() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar')

        String contents = 'new Flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'Flar', 1)
    }

    @Test
    void testNamedArgs3() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = 'new Flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'Flar', 1)
    }

    @Test // some args filled in
    void testNamedArgs4() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar')

        String contents = 'new Flar(aaa:9)'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'Flar', 1)
    }

    @Test // some args filled in
    void testNamedArgs5() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar')

        String contents = 'new Flar(bbb:7, aaa:9)'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'Flar', 1)
    }

    @Test // some args filled in
    void testNamedArgs6() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar')

        String contents = 'new Flar(bbb:7, ccc:8, aaa:9)'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 0)
        proposalExists(proposals, 'bbb : __', 0)
        proposalExists(proposals, 'ccc : __', 0)
        proposalExists(proposals, 'Flar', 1)
    }

    @Test // STS-2628: ensure no double adding of named properties for booleans
    void testNamedArgs7() {
        addGroovySource('''\
            class Flar {
              boolean aaa
              boolean bbb
              boolean ccc
            }
            '''.stripIndent(), 'Flar')

        String contents = 'new Flar()'
        ICompletionProposal[] proposals = createProposalsAtOffset(contents, getLastIndexOf(contents, '('))
        proposalExists(proposals, 'aaa : __', 1)
        proposalExists(proposals, 'bbb : __', 1)
        proposalExists(proposals, 'ccc : __', 1)
        proposalExists(proposals, 'Flar', 1)
    }

    @Test
    void testParamGuessing1() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            int yyy
            boolean zzz
            new Flar()
            '''.stripIndent()
        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __, ', expectedChoices)
    }

    @Test
    void testParamGuessing2() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            String xxx
            int yyy
            boolean zzz
            new p.Flar()
            '''.stripIndent()
        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __, ', expectedChoices)
    }

    @Test
    void testParamGuessing3() {
        addGroovySource('''\
            class Flar {
              String aaa
              int bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            Integer yyy
            boolean zzz
            new Flar()
            '''.stripIndent()
        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __, ', expectedChoices)
    }

    @Test
    void testParamGuessing4() {
        addGroovySource('''\
            class Flar {
              String aaa
              Integer bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            Integer yyy
            boolean zzz
            new Flar()
            '''.stripIndent()
        String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __, ', expectedChoices)
    }

    @Test
    void testParamGuessing5() {
        addGroovySource('''\
            class Flar {
              String aaa
              Integer bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            int yyy
            boolean zzz
            new Flar()
            '''.stripIndent()
      String[] expectedChoices = [ 'yyy', '0' ]
        checkProposalChoices(contents, 'Flar(', 'bbb', 'bbb: __, ', expectedChoices)
    }

    @Test
    void testParamGuessing6() {
        addGroovySource('''\
            class Flar {
              String aaa
              Integer bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            String xxx
            int yyy
            boolean zzz
            new Flar()
            '''.stripIndent()
        String[] expectedChoices = [ 'xxx', '""' ]
        checkProposalChoices(contents, 'Flar(', 'aaa', 'aaa: __, ', expectedChoices)
    }

    @Test
    void testParamGuessing7() {
        addGroovySource('''\
            class Flar {
              Closure aaa
              Integer bbb
              Date ccc
            }
            '''.stripIndent(), 'Flar', 'p')

        String contents = '''\
            import p.Flar
            Closure xxx
            int yyy
            boolean zzz
            new Flar()
            '''.stripIndent()
        String[] expectedChoices = [ 'xxx', '{  }' ]
        checkProposalChoices(contents, 'Flar(', 'aaa', 'aaa: __, ', expectedChoices)
    }
}
