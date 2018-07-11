package io.github.loveginger.library.glidebitmappool;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class) public class ExampleUnitTest {

  @Mock List mockList2;

  @Spy List<String> spyList2 = new ArrayList<>();

  @Captor ArgumentCaptor argumentCaptor2;

  @Test public void addition_isCorrect() {
    assertEquals(4, 2 + 2);
  }

  @Test public void mock1() {
    List mockList = mock(ArrayList.class);
    mockList.add("1");
    verify(mockList).add("1");
    assertThat(mockList.size(), is(0));

    when(mockList.size()).thenReturn(100);
    assertThat(mockList.size(), is(100));
  }

  @Test public void mock2() {
    mockList2 = mock(ArrayList.class);
    mockList2.add("1");
    verify(mockList2).add("1");
    assertThat(mockList2.size(), is(0));

    when(mockList2.size()).thenReturn(100);
    assertThat(mockList2.size(), is(100));
  }

  @Test public void spy1() {
    List<String> spyList = spy(new ArrayList<String>());
    spyList.add("1");
    spyList.add("2");
    assertThat(spyList.size(), is(2));

    when(spyList.size()).thenReturn(100);
    assertThat(spyList.size(), is(100));
  }

  @Test public void spy2() {
    spyList2 = spy(new ArrayList<String>());
    spyList2.add("1");
    spyList2.add("2");
    assertThat(spyList2.size(), is(2));

    when(spyList2.size()).thenReturn(100);
    assertThat(spyList2.size(), is(100));
  }

  @Test public void captor2() {
    mockList2.add("one");
    verify(mockList2).add(argumentCaptor2.capture());

    assertThat((String) argumentCaptor2.getValue(), is("one"));
  }
}