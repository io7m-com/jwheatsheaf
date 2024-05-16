/*
 * Copyright © 2024 Mark Raynsford <code@io7m.com> https://www.io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

public final class Tools
{
  private static final TreeMap<String, OpType> OPS =
    new TreeMap<>();

  static {
    final var opsList = List.of(
      new ShowProjectVersion(),
      new ShowProjectIsSnapshot()
    );

    for (final var op : opsList) {
      OPS.put(op.name(), op);
    }
  }

  private Tools()
  {

  }

  private interface OpType
  {
    String name();

    void execute(String[] args)
      throws Exception;
  }

  private static String getProjectVersion(
    final File file)
    throws Exception
  {
    final var documentBuilders =
      DocumentBuilderFactory.newInstance();
    final var documentBuilder =
      documentBuilders.newDocumentBuilder();
    final var document =
      documentBuilder.parse(file);

    final var xPathFactory =
      XPathFactory.newInstance();
    final var xPath =
      xPathFactory.newXPath();

    final var nodes =
      (NodeList) xPath.evaluate(
        "//project/version",
        document,
        XPathConstants.NODESET
      );

    for (var i = 0; i < nodes.getLength(); i++) {
      final var node = nodes.item(i);
      if (node.getNodeType() == Node.ELEMENT_NODE) {
        return node.getTextContent().trim();
      }
    }

    throw new IOException(
      "Could not locate a //project/version node!"
    );
  }

  private static final class ShowProjectVersion implements OpType
  {
    ShowProjectVersion()
    {

    }

    @Override
    public String name()
    {
      return "ShowProjectVersion";
    }

    @Override
    public void execute(
      final String[] args)
      throws Exception
    {
      System.out.print("IO7M_PROJECT_VERSION=");
      System.out.println(getProjectVersion(new File(args[1])));
    }
  }

  private static final class ShowProjectIsSnapshot implements OpType
  {
    ShowProjectIsSnapshot()
    {

    }

    @Override
    public String name()
    {
      return "ShowProjectIsSnapshot";
    }

    @Override
    public void execute(
      final String[] args)
      throws Exception
    {
      System.out.print("IO7M_PROJECT_VERSION_IS_SNAPSHOT=");
      System.out.println(
        getProjectVersion(new File(args[1])).endsWith("-SNAPSHOT")
      );
    }
  }

  public static void main(
    final String[] args)
    throws Exception
  {
    if (args.length == 0) {
      System.err.println("Usage: command");
      System.exit(1);
    }

    final var op = OPS.get(args[0]);
    if (op == null) {
      System.err.println("Unrecognized command.");
      System.err.println("  Must be one of:");
      for (final var name : OPS.keySet()) {
        System.err.print("    ");
        System.err.println(name);
      }
      System.exit(1);
    }

    op.execute(args);
  }
}