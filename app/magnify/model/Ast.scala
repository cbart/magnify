package magnify.model

/**
 * @author Cezary Bartoszuk (cezarybartoszuk@gmail.com)
 */
final case class Ast(
    imports: Seq[String],
    className: String,
    asteriskPackages: Seq[String],
    unresolvedClasses: Seq[String])
