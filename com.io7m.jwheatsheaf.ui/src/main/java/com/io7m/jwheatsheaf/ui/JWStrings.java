/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.jwheatsheaf.ui;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * The default application string provider.
 */

public final class JWStrings
{
  private final ResourceBundle resourceBundle;

  private JWStrings(
    final ResourceBundle inResourceBundle)
  {
    this.resourceBundle = inResourceBundle;
  }

  /**
   * Retrieve the resource bundle for the given locale.
   *
   * @param locale The locale
   *
   * @return The resource bundle
   */

  public static ResourceBundle getResourceBundle(
    final Locale locale)
  {
    return ResourceBundle.getBundle(
      "com.io7m.jwheatsheaf.ui.Strings",
      locale);
  }

  /**
   * Retrieve the resource bundle for the current locale.
   *
   * @return The resource bundle
   */

  public static ResourceBundle getResourceBundle()
  {
    return getResourceBundle(Locale.getDefault());
  }

  /**
   * Create a new string provider from the given bundle.
   *
   * @param bundle The resource bundle
   *
   * @return A string provider
   */

  public static JWStrings of(
    final ResourceBundle bundle)
  {
    return new JWStrings(bundle);
  }

  public String createDirectoryTitle()
  {
    return this.resourceBundle.getString("ui.directoryCreateTitle");
  }

  public String recentItems()
  {
    return this.resourceBundle.getString("ui.recentItems");
  }

  public String filterAllFilesDescription()
  {
    return this.resourceBundle.getString("ui.filterAllFilesDescription");
  }

  public String filterOnlyDirectoriesDescription()
  {
    return this.resourceBundle.getString("ui.filterOnlyDirectoriesDescription");
  }

  public String enterDirectoryName()
  {
    return this.resourceBundle.getString("ui.directoryName");
  }

  public String tooltipNavigateDirectory()
  {
    return this.resourceBundle.getString("ui.tooltip.directory");
  }

  public String fileSelect()
  {
    return this.resourceBundle.getString("ui.fileSelect");
  }

  public String filesSelect()
  {
    return this.resourceBundle.getString("ui.filesSelect");
  }

  public String open()
  {
    return this.resourceBundle.getString("ui.open");
  }

  public String save()
  {
    return this.resourceBundle.getString("ui.save");
  }

  public String enterPathTitle()
  {
    return this.resourceBundle.getString("ui.enterPathTitle");
  }

  public String enterPath()
  {
    return this.resourceBundle.getString("ui.enterPath");
  }
}
