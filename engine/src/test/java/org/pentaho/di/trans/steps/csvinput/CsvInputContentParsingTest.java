/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.csvinput;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class CsvInputContentParsingTest extends BaseCsvParsingTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testDefaultOptions() throws Exception {
    init( "default.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
        new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" } } );
  }

  @Test
  public void testColumnNameWithSpaces() throws Exception {
    init( "column_name_with_spaces.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
        new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" } } );
  }

  @Test
  public void testSemicolonOptions() throws Exception {
    meta.setDelimiter( ";" );
    init( "semicolon.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
        new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" }, {
        "\u043d\u0435-\u043b\u0430\u0446\u0456\u043d\u043a\u0430(non-latin)", "4", "4" } } );
  }

  @Test
  public void testMultiCharDelimOptions() throws Exception {
    meta.setDelimiter( "|||" );
    init( "multi_delim.csv" );

    setFields( new TextFileInputField( "Field 1", -1, -1 ), new TextFileInputField( "Field 2", -1, -1 ),
        new TextFileInputField( "Field 3", -1, -1 ) );

    process();

    check( new Object[][] { { "first", "1", "1.1" }, { "second", "2", "2.2" }, { "third", "3", "3.3" }, {
        "\u043d\u0435-\u043b\u0430\u0446\u0456\u043d\u043a\u0430(non-latin)", "4", "4" } } );
  }

  @Test
  public void testMixFileFormat() throws Exception {
    String data = "?????????1,?????????2,?????????3,?????????4\n"
      + "111,\"a\n"
      + "bc\",?????????,?????????\n"
      + "222,def,?????????,?????????\r\n"
      + "333,,?????????,?????????\n"
      + "444,,\n"
      + "555,?????????,\r\n"
      + "666,?????????\r\n"
      + "\n"
      + "777,\n"
      + "888,?????????\r\n"
      + "\n"
      + "999,123,123,123,132,132,132,132,132\r";

    String file = createTestFile( "UTF-8", data ).getAbsolutePath();
    init( file, true );

    setFields( new TextFileInputField( "Col 1", -1, -1 ), new TextFileInputField( "Col 2", -1, -1 ),
      new TextFileInputField( "Col 3", -1, -1 ), new TextFileInputField( "Col 4", -1, -1 ),
      new TextFileInputField( "Col 5", -1, -1 ) );

    process();

    check( new Object[][] {
      { "111", "a\nbc", "?????????", "?????????", null },
      { "222", "def", "?????????", "?????????", null},
      { "333", "", "?????????", "?????????", null },
      { "444", "", "", null, null },
      { "555", "?????????", "", null, null },
      { "666", "?????????", null, null, null},
      { },
      { "777", "", null, null, null },
      { "888", "?????????", null, null, null },
      { },
      { "999", "123", "123", "123", "132" } }
    );
  }

  @Test( expected = KettleStepException.class )
  public void testNoHeaderOptions() throws Exception {
    meta.setHeaderPresent( false );
    init( "default.csv" );

    setFields( new TextFileInputField(), new TextFileInputField(), new TextFileInputField() );

    process();
  }

  File createTestFile( final String encoding, final String content ) throws IOException {
    File tempFile = File.createTempFile( "PDI_tmp", ".csv" );
    tempFile.deleteOnExit();

    try ( PrintWriter osw = new PrintWriter( tempFile, encoding ) ) {
      osw.write( content );
    }

    return tempFile;
  }
}
