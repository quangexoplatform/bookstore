package org.exoplatform.test.utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Value;

/**
 * A simple util wrapper to read JCR Nodes properties easily.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 *
 */
public class PropertyReader {

  Node node = null;

  public PropertyReader(Node node) {
    this.node = node;
  }

  public Double d(String name) {
    try {
      return node.getProperty(name).getDouble();
    } catch (Exception e) {
      return 0d;
    }
  }

  public long l(String name) {
    return l(name, 0);
  }

  public long l(String name, long l) {
    try {
      return node.getProperty(name).getLong();
    } catch (Exception e) {
      return l;
    }
  }

  public String string(String name, String defaultValue) {
    try {
      return node.getProperty(name).getString();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public String string(String name) {
    return string(name, null);
  }

  public Date date(String name) {
    return date(name, null);
  }

  public Date date(String name, Date defaultValue) {
    try {
      return node.getProperty(name).getDate().getTime();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public Boolean bool(String name) {
    return bool(name, false);
  }

  public Boolean bool(String name, boolean defaultValue) {
    try {
      return node.getProperty(name).getBoolean();
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public String[] strings(String name) {
    return strings(name, null);
  }

  public String[] strings(String name, String[] defaultValue) {
    try {
      return valuesToArray(node.getProperty(name).getValues());
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public List<String> list(String name) {
    return list(name, null);
  }

  public List<String> list(String name, List<String> defaultValue) {
    try {
      Value[] values = node.getProperty(name).getValues();
      return valuesToList(values);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  public Set<String> set(String name) {
    return set(name, null);
  }

  public Set<String> set(String name, Set<String> defaultValue) {
    try {
      Value[] values = node.getProperty(name).getValues();
      Set<String> result = new HashSet<String>();
      result.addAll(valuesToList(values));
      return result;
    } catch (Exception e) {
      return defaultValue;
    }
  }

  private String[] valuesToArray(Value[] Val) throws Exception {
    if (Val.length < 1)
      return new String[] {};
    List<String> list = valuesToList(Val);
    return list.toArray(new String[list.size()]);
  }

  private List<String> valuesToList(Value[] values) throws Exception {
    List<String> list = new ArrayList<String>();
    if (values.length < 1)
      return list;
    String s;
    for (int i = 0; i < values.length; ++i) {
      s = values[i].getString();
      if (s != null && s.trim().length() > 0)
        list.add(s);
    }
    return list;
  }

}
