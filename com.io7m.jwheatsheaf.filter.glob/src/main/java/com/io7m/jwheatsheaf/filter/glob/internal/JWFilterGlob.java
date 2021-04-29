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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

public final class JWFilterGlob implements JWFileChooserFilterType
{
  private final String description;
  private final List<JWFilterGlobRule> rules;

  public JWFilterGlob(
    final String inDescription,
    final List<JWFilterGlobRule> inRules)
  {
    this.description =
      Objects.requireNonNull(inDescription, "description");
    this.rules =
      Objects.requireNonNull(inRules, "rules");
  }

  @Override
  public String description()
  {
    return this.description;
  }

  @Override
  public boolean isAllowed(
    final Path path)
  {
    final var filesystem = path.getFileSystem();

    var included = false;
    for (final var rule : this.rules) {
      final var matcher =
        filesystem.getPathMatcher(String.format("glob:%s", rule.pattern()));
      final var matches =
        matcher.matches(path);

      if (matches) {
        switch (rule.kind()) {
          case INCLUDE:
            included = true;
            break;
          case EXCLUDE:
            included = false;
            break;
          case INCLUDE_AND_HALT:
            return true;
          case EXCLUDE_AND_HALT:
            return Files.isDirectory(path);
        }
      }
    }

    return included || Files.isDirectory(path);
  }
}
