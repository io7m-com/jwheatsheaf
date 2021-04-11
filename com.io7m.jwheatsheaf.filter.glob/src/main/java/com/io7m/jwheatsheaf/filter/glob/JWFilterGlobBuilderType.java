/*
 * Copyright © 2021 Mark Raynsford <code@io7m.com> http://io7m.com
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

package com.io7m.jwheatsheaf.filter.glob;

import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;

/**
 * The type of glob filter builders.
 *
 * A glob filter works by executing a series of rules on incoming filenames.
 * The rules are executed against each incoming path in the order that they
 * were added to the filter with {@code addRule}. A path that matches none of
 * the rules is excluded, unless the path refers to a directory.
 */

public interface JWFilterGlobBuilderType
{
  /**
   * Add a rule to the filter.
   *
   * @param kind    The kind of rule
   * @param pattern The glob pattern
   *
   * @return this
   */

  JWFilterGlobBuilderType addRule(
    JWFilterGlobRuleKind kind,
    String pattern
  );

  /**
   * @return An immutable filter based on all of the rules given so far
   */

  JWFileChooserFilterType build();
}
