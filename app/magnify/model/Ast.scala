package magnify.model

import java.util.Objects

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final case class Ast(
    className: String,
    imports: Set[String],
    asteriskPackages: Set[String],
    unresolvedClasses: Set[String])
