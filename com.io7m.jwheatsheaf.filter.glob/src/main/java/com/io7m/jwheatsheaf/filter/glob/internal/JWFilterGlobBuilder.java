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

package com.io7m.jwheatsheaf.filter.glob.internal;

import com.io7m.jwheatsheaf.api.JWFileChooserFilterType;
import com.io7m.jwheatsheaf.filter.glob.JWFilterGlobBuilderType;
import com.io7m.jwheatsheaf.filter.glob.JWFilterGlobRuleKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class JWFilterGlobBuilder implements JWFilterGlobBuilderType
{
  private final String description;
  private final ArrayList<JWFilterGlobRule> rules;

  public JWFilterGlobBuilder(
    final String inDescription)
  {
    this.description =
      Objects.requireNonNull(inDescription, "description");
    this.rules =
      new ArrayList<>();
  }

  @Override
  public JWFilterGlobBuilderType addRule(
    final JWFilterGlobRuleKind kind,
    final String pattern)
  {
    Objects.requireNonNull(kind, "kind");
    Objects.requireNonNull(pattern, "pattern");

    this.rules.add(JWFilterGlobRule.of(kind, pattern));
    return this;
  }

  @Override
  public JWFileChooserFilterType build()
  {
    return new JWFilterGlob(this.description, List.copyOf(this.rules));
  }
}
