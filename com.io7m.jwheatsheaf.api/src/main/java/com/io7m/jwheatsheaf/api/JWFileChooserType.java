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

package com.io7m.jwheatsheaf.api;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/**
 * A created file chooser.
 */

public interface JWFileChooserType
{
  /**
   * Add a receiver of events. The given receiver is called whenever the
   * file chooser publishes an event that may need the caller to perform
   * some action (such as display an error dialog box). The receiver is
   * guaranteed to be called on the FX application thread.
   *
   * @param receiver The receiver
   */

  void setEventListener(Consumer<JWFileChooserEventType> receiver);

  /**
   * Display the file chooser and wait until the user either selects files,
   * or cancels the dialog.
   *
   * @return The selected files, if any
   */

  List<Path> showAndWait();

  /**
   * Display the file chooser and return immediately.
   */

  void show();

  /**
   * @return The selected files, if any
   */

  List<Path> result();

  /**
   * If the file chooser is open, then hide it and behave as if the user
   * cancelled the selection.
   */

  void cancel();
}
