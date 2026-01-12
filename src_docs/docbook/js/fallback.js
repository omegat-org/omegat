/* DocBook xslTNG version 2.5.0
 *
 * This is fallback.js providing support for audio/video fallback.
 *
 * See https://xsltng.docbook.org/
 *
 */
function docbook_object_fallback(node) {
  let markup = "<p class='fallback'>Playback not supported. Download ";
  let sources = node.querySelectorAll("source");
  let pos = 1;
  sources.forEach(source => {
    if (pos > 2 && pos == sources.length) {
      markup += ", or ";
    } else if (pos == 2 && pos == sources.length) {
      markup += " or ";
    } else if (pos > 1) {
      markup += ", ";
    }
    markup += `<a href='${source.getAttribute('src')}'>${source.getAttribute('type')}</a>`;
    pos++;
  });
  markup += ".</p>";
  node.parentNode.innerHTML = markup;
}
