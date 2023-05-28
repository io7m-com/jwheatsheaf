/*
 * Copyright © 2020 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.jwheatsheaf.examples;

import com.io7m.jwheatsheaf.api.JWFileImageSetType;
import com.io7m.jwheatsheaf.oxygen.JWOxygenIconSet;
import com.io7m.jwheatsheaf.ui.JWFileChoosers;

import java.util.Map;
import java.util.Objects;

/**
 * The available image sets.
 */

public final class ExampleImageSets
{
  private final Map<String, JWFileImageSetType> imageSets;

  /**
   * Construct image sets.
   *
   * @param inImageSets The image sets
   */

  public ExampleImageSets(
    final Map<String, JWFileImageSetType> inImageSets)
  {
    this.imageSets = Objects.requireNonNull(inImageSets, "imageSets");
  }

  /**
   * Create a new set of image sets.
   *
   * @return The image sets
   */

  public static ExampleImageSets create()
  {
    return new ExampleImageSets(
      Map.ofEntries(
        Map.entry("Default", JWFileChoosers.createDefaultIcons()),
        Map.entry("Inverse", new ExampleInverseIconSet()),
        Map.entry("Oxygen", new JWOxygenIconSet())
      )
    );
  }

  /**
   * @return The available image sets
   */

  public Map<String, JWFileImageSetType> imageSets()
  {
    return this.imageSets;
  }
}
