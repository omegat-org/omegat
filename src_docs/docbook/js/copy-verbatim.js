/* DocBook xslTNG version 2.5.0
 *
 * This is copy-verbatim.js providing support for a dynamic
 * copy-to-clipboard link on verbatim listings.
 *
 * See https://xsltng.docbook.org/
 *
 */
(function() {
  const COPY = "<path d='M0 6.75C0 5.784.784 5 1.75 5h1.5a.75.75 0 0 1 0 1.5h-1.5a.25.25 0 0 0-.25.25v7.5c0 .138.112.25.25.25h7.5a.25.25 0 0 0 .25-.25v-1.5a.75.75 0 0 1 1.5 0v1.5A1.75 1.75 0 0 1 9.25 16h-7.5A1.75 1.75 0 0 1 0 14.25Z'></path><path d='M5 1.75C5 .784 5.784 0 6.75 0h7.5C15.216 0 16 .784 16 1.75v7.5A1.75 1.75 0 0 1 14.25 11h-7.5A1.75 1.75 0 0 1 5 9.25Zm1.75-.25a.25.25 0 0 0-.25.25v7.5c0 .138.112.25.25.25h7.5a.25.25 0 0 0 .25-.25v-7.5a.25.25 0 0 0-.25-.25Z'></path>";
  const OK = "<path d='M13.78 4.22a.75.75 0 0 1 0 1.06l-7.25 7.25a.75.75 0 0 1-1.06 0L2.22 9.28a.751.751 0 0 1 .018-1.042.751.751 0 0 1 1.042-.018L6 10.94l6.72-6.72a.75.75 0 0 1 1.06 0Z'></path>";

  let text = "";

  const buildText = function(elem) {
    const nodes = elem.childNodes;
    for (let pos = 0; pos < nodes.length; pos++) {
      const child = nodes[pos];
      switch (child.nodeType) {
      case Node.ELEMENT_NODE:
        if (child.classList.contains("callout-bug")
            || child.classList.contains("co")
            || child.classList.contains("lineannotation")
            || child.classList.contains("ln")
            || child.classList.contains("nsep")) {
          // Ignore these
        } else {
          buildText(child);
        }
        break;
      case Node.TEXT_NODE:
        text += child.textContent;
        break;
      default:
        break;
      }
    }
  };

  const copyText = function(event) {
    // The target could be anywhere inside the SVG. Go up until we find the div.
    // Keep track of the "svg" element along the way.
    let div = event.target;
    let svg = null;
    while (div && div.tagName !== "DIV") {
      if (div.tagName == "svg") { // Yes, lower case!
        svg = div;
      }
      div = div.parentNode;
    }

    const table = div.querySelector("table");
    let pre = null;
    if (table) {
      div.querySelectorAll("pre").forEach(elem => {
        pre = elem;
      });
    } else {
      pre = div.querySelector("pre");
    }

    if (!pre) {
      console.log("Error: no <pre> to copy!?");
      return;
    }

    text = "";
    buildText(pre);

    // Trim away trailing blanks
    text = text.replaceAll(/[ \t]+\n/g, "\n");
    navigator.clipboard.writeText(text);

    // Remove the event listener while we display the checkbox
    svg.removeEventListener("click", copyText);

    // Display the checkbox for a moment
    svg.innerHTML = OK;
    svg.style.fill = "#006400";
    setTimeout(() => {
      svg.innerHTML = COPY;
      svg.style.fill = "#000000";
      svg.addEventListener("click", copyText);
    }, 1000);
  };

  const showCopyButton = function(event) {
    const div = event.target;
    const pre = div.querySelector("pre");
    if (!pre) {
      return;
    }

    let svg = div.querySelector(".copyVerbIcon");
    if (svg) {
      svg.style.display = "block";
    } else {
      let svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
      svg.setAttribute("aria-hidden", "true");
      svg.setAttribute("width", "16");
      svg.setAttribute("height", "16");
      svg.setAttribute("viewBox", "0 0 16 16");
      svg.setAttribute("version", "1.1");
      svg.style.position = "absolute";
      svg.style.top = "2px";
      svg.style.right = "2px";
      svg.style.padding = "4px";
      svg.style.fill = "black";
      svg.classList.add("copyVerbIcon");
      svg.addEventListener("click", copyText);
      svg.innerHTML = COPY;
      div.insertBefore(svg, div.firstChild);
    }
  };

  const hideCopyButton = function(event) {
    const div = event.target;
    const span = div.querySelector(".copyVerbIcon");
    if (span) {
      span.style.display = "none";
    }
  };

  // Navigator.clipboard requires a localhost or https: connection
  // If we can't change the clipboard, then just forget it.
  if (navigator.clipboard) {
    document.querySelectorAll(".pre-wrap").forEach(div => {
      div.style.position="relative";
      div.addEventListener("mouseenter", showCopyButton);
      div.addEventListener("mouseleave", hideCopyButton);
    });
  }
})();
