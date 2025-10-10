///
/// This package contains a port of https://github.com/mattvchandler/textogl
/// to Java.
///
/// Original License: MIT
///
/// Notable changes:
/// - integration with Minecraft's rendering engine, therefore the buffer management is left out.
/// - Slightly optimized font atlas packing
/// - support for Minecraft's formatted text system (all formatting codes/styling options except for custom fonts obviously)
/// - support for variable fonts
///
/// Version-specific note: This package also contains some utilities to improve font handling with
/// this old game version, therefore the note above applies to the `Font` class.
///
package io.github.axolotlclient.rendering.font;

