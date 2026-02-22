/* DocBook xslTNG version 2.5.0
 *
 * This is pagetoc.js providing support for on-page ToCs.
 *
 * See https://xsltng.docbook.org/
 *
 */

(function() {
  const DECORATED = 0;
  const PLAIN = 1;
  const HIDDEN = 2;

  const html = document.querySelector("html");
  const main = document.querySelector("main");
  const pagetoc = document.querySelector("nav.pagetoc");
  const tocwrap = document.querySelector("nav.pagetoc div.tocwrapper");

  const onerem = parseFloat(getComputedStyle(html).fontSize);
  const mainMinWidthStyle = getComputedStyle(main).minWidth;
  const mainMaxWidthStyle = getComputedStyle(main).maxWidth;
  const mainMinWidth = parseInt(mainMinWidthStyle.substring(0, mainMinWidthStyle.length - 2));
  const mainMaxWidth = parseInt(mainMaxWidthStyle.substring(0, mainMaxWidthStyle.length - 2));

  const pagetocWidthStyle = getComputedStyle(pagetoc).width;
  const pagetocMinWidth = parseInt(pagetocWidthStyle.substring(0, pagetocWidthStyle.length - 2));

  let uncentered = true;
  let dynamic = true;
  html.querySelectorAll("head script").forEach(script => {
    const data = script.getAttribute("data-dynamic-pagetoc");
    if (data) {
      dynamic = (data == "true");
    }
  });

  let sections = [];
  let tocstate = dynamic ? DECORATED : PLAIN;
  let tocclass = "active";
  let toclength = 0;
  let cbcount = 0;
  let hidden_section = false;
  let nothing_to_reveal = HIDDEN;
  let idcount = 0;
  let onscreen = false;

  const randomId = function() {
    idcount++;
    return `__random_${idcount}`;
  };

  const findsections = function(parent) {
    const sections = [];
    const ancestors = [];
    let firstparent = null;
    parent.querySelectorAll(":scope > article,:scope > section").forEach(sect => {
      if (!sect.hasAttribute("id")) {
        sect.setAttribute("id", randomId());
      }
      const id = sect.getAttribute("id");
      const header = sect.querySelector("header");
      const skip = sect.classList.contains("nopagetoc");

      let title = header && header.querySelector("h1,h2,h3,h4,h5,h6");
      if (title.querySelector("script.titleabbrev")) {
        title = title.querySelector("script.titleabbrev");
      }

      if (title && !skip) {
        toclength++;
        sections.push({
          "elem": sect,
          "id": id,
          "title": title.innerHTML
        });
      }
    });

    return sections;
  };

  const maketoc = function(sections, depth) {
    if (depth == 0) {
      if (dynamic) {
        const ctrl = document.createElement("div");
        ctrl.setAttribute("class", "ctrl");
        const btn = document.createElement("span");
        btn.setAttribute("class", "toggle");
        btn.innerHTML = window.DocBook.pagetoc.decorated;
        ctrl.appendChild(btn);
        ctrl.addEventListener("click", event => {
          switch (tocstate) {
          case DECORATED:
            tocstate = PLAIN;
            btn.innerHTML = window.DocBook.pagetoc.plain;
            tocwrap.querySelectorAll(".li.active").forEach(li => {
              li.classList["remove"]('active');
              li.classList["add"]('plain');
            });
            tocclass = 'plain';
            break;
          case PLAIN:
            tocstate = HIDDEN;
            btn.innerHTML = window.DocBook.pagetoc.hidden;
            tocwrap.style.display = "none";
            break;
          case HIDDEN:
            tocstate = DECORATED;
            btn.innerHTML = window.DocBook.pagetoc.decorated;
            tocwrap.style.display = "block";
            tocwrap.querySelectorAll(".li.plain").forEach(li => {
              li.classList["remove"]('plain');
              li.classList["add"]('active');
            });
            tocclass = 'active';
            break;
          }
        });
        tocwrap.parentNode.insertBefore(ctrl, tocwrap);
      }
    }

    sections.forEach(section => {
      const subsections = findsections(section.elem);
      const div = document.createElement("div");
      div.setAttribute("class", "li depth"+depth);
      const anchor = document.createElement("a");
      anchor.setAttribute("href", "#" + section.id);
      anchor.innerHTML = section.title;
      div.appendChild(anchor);
      tocwrap.appendChild(div);
      if (subsections) {
        maketoc(subsections, depth+1);
      }
    });
  };

  const nothingToReveal = function() {
    if (nothing_to_reveal == HIDDEN) {
      pagetoc.style.display = "none";
      return;
    }

    let addRemove = nothing_to_reveal == PLAIN ? "remove" : "add";
    sections.forEach((section) => {
      const id = section.id;
      if (id) {
        // Get the link to this section's heading
        const link = tocwrap.querySelector(`nav.pagetoc .li a[href="#${id}"]`);
        if (link) {
          link.parentNode.classList[addRemove](tocclass);
        }
      }
    });
  };

  const scrollHandler = function(event) {
    if (pagetoc.scrollHeight <= pagetoc.clientHeight) {
      // No scrolling is necessary
      return;
    }

    let lastActiveIndex = 0;
    let lastActiveDiv = null;
    pagetoc.querySelectorAll("div.li").forEach((div, index) => {
      if (div.classList.contains("active")) {
        lastActiveIndex = index;
        lastActiveDiv = div;
      }
    });

    if (lastActiveDiv == null) {
      // IntersectionObserver hasn't fired yet after a reload.
      return;
    }

    let offset = pagetoc.clientHeight / 4;
    if (lastActiveDiv.offsetTop > offset) {
      pagetoc.scrollTo(0, lastActiveDiv.offsetTop - offset);
    } else {
      pagetoc.scrollTo(0, 0);
    }
  };

  const centerMain = function() {
    // If the pagetoc is not displayed, just leave everything alone
    if (getComputedStyle(pagetoc).display == "none") {
      if (onscreen) {
        main.style.marginLeft = "auto";
        main.style.marginRight = "auto";
        main.style.paddingLeft = "0";
        main.style.minWidth = mainMinWidthStyle;
        main.style.maxWidth = mainMaxWidthStyle;
      }
      onscreen = false;
      return;
    }
    onscreen = true;

    // Some padding
    const pad = 4*onerem;

    // Compute available width and new main width
    let availableWidth = html.clientWidth - (pagetocMinWidth + pad);
    let newWidth = Math.min(mainMaxWidth, availableWidth);

    // Let the tocwidth grow if there's already more than enough room for main
    let tocwidth = html.clientWidth - (newWidth + pad);
    tocwidth = Math.max(tocwidth, pagetocMinWidth);
    tocwidth = Math.min(tocwidth, (pagetocMinWidth * 1.5));
    pagetoc.style.width = `${tocwidth}px`;

    // Recompute the available width and main width (in case we changed the pagetoc width)
    availableWidth = html.clientWidth - pagetoc.clientWidth;
    newWidth = Math.min(mainMaxWidth, availableWidth - (2*pad));

    let paddingLeft = Math.trunc((availableWidth - newWidth) / 2);

    const nwStr = `${newWidth}px`;
    main.style.width = nwStr;
    main.style.minWidth = nwStr;
    main.style.marginLeft = "0";
    main.style.paddingLeft = `${paddingLeft}px`;
  };

  const resizeHandler = function(event) {
    centerMain();
  };

  if (main && pagetoc) {
    if (!window.DocBook) {
      window.DocBook = {};
    }
    if (!window.DocBook.pagetoc) {
      window.DocBook.pagetoc = {};
    }
    if (!window.DocBook.pagetoc.decorated) {
      window.DocBook.pagetoc.decorated = "☀";
    }
    if (!window.DocBook.pagetoc.plain) {
      window.DocBook.pagetoc.plain = "￮";
    }
    if (!window.DocBook.pagetoc.hidden) {
      window.DocBook.pagetoc.hidden = "◄";
    }

    if (window.DocBook.pagetoc.nothing_to_reveal) {
      if (window.DocBook.pagetoc.nothing_to_reveal === "hide") {
        nothing_to_reveal = HIDDEN;
      } else if (window.DocBook.pagetoc.nothing_to_reveal === "plain") {
        nothing_to_reveal = PLAIN;
      } else {
        nothing_to_reveal = DECORATED;
      }
    }

    sections = findsections(main);
    maketoc(sections, 0);

    if (toclength > 1) {
      if (dynamic) {
        const observer = new IntersectionObserver((sections) => {
          sections.forEach((section) => {
            const id = section.target.getAttribute("id");
            const header = section.target.querySelector("header");
            const title = header && header.querySelector("h1,h2,h3,h4,h5,h6");
            if (!id || !title) {
              return;
            }

            // Get the link to this section's heading
            const link = tocwrap.querySelector(`nav.pagetoc .li a[href="#${id}"]`);
            if (!link) {
              return;
            }

            // Add/remove the .active class based on whether the
            // section is visible
            const addRemove = section.intersectionRatio > 0 ? 'add' : 'remove';
            link.parentNode.classList[addRemove](tocclass);

            hidden_section = hidden_section || (addRemove === "remove");
            cbcount++;

            if (cbcount == toclength) {
              // We've made a complete pass. If there's nothing hidden,
              // then there's nothing to reveal...
              if (!hidden_section) {
                nothingToReveal();
              } else {
                // Otherwise, the first time through, center main
                if (uncentered) {
                  centerMain();
                  uncentered = false;
                }
              }
            }
          });
        });

        // Observe all the sections of the article
        main.querySelectorAll('article,section').forEach((section) => {
          observer.observe(section);
        });
      }

      window.addEventListener("scroll", scrollHandler);
      window.addEventListener("resize", resizeHandler);
    } else {
      pagetoc.style.display = "none";
    }
  }
})();
