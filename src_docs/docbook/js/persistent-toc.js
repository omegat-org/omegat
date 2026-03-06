/* DocBook xslTNG version 2.5.0
 *
 * This is persistent-toc.js providing support for the ToC popup
 *
 * See https://xsltng.docbook.org/
 *
 */

(function() {
  const ESC = 27;
  const SPACE = 32;
  const toc = document.querySelector("nav.toc");
  let tocPersist = null;
  let borderLeftColor = "white";
  let curpress = null;
  let searchListener = false;
  let VERSION = "2.5.0";
  let PTOCID = "ptoc-data-file";

  const showToC = function(event) {
    toc.style.width = "300px";
    toc.style["padding-left"] = "1em";
    toc.style["padding-right"] = "1em";
    toc.style["border-left"] = `1px solid ${borderLeftColor}`;

    // Make sure the tocPersist checkbox is created
    tocPersistCheckbox(event && event.shiftKey);

    if (event) {
      event.preventDefault();
    }

    // Do we need to load the ToC?
    let div = toc.querySelector("div");
    let prefix = div.getAttribute("db-prefix") || "";
    if (div && div.getAttribute("db-persistent-toc")) {
      let path = prefix + div.getAttribute("db-persistent-toc");
      let uri = new URL(path, document.location).href;
      fetch(uri)
        .then((response) => {
          if (response.status == 200) {
            response.text()
              .then(text => {
                let doc = new DOMParser().parseFromString(text, "text/html");
                let body = doc.querySelector("body");
                div.innerHTML = body.innerHTML;
                processToC(event);
              })
              .catch(err => {
                // I don't think this error can actually occur with the string parser
                // using the text/html mimeType. But just in case...
                let href = `https://xsltng.docbook.org/guide/${VERSION}/ch-using.html#${PTOCID}`;
                let resp = `Error: <a class="showlink" href='${href}'>persistent ToC</a>`;
                div.innerHTML = `${resp} data could not be parsed.`;
              });
          } else {
            let href = `https://xsltng.docbook.org/guide/${VERSION}/ch-using.html#${PTOCID}`;
            let resp = `Error: <a class="showlink" href='${href}'>persistent ToC</a>`;
            if (response.status == 404) {
              div.innerHTML = `${resp} data file not found.`;
            } else {
              div.innerHTML = `${resp} could not read data file (status: ${response.status}).`;
            }
          }
        })
        .catch(err => {
          let href = `https://xsltng.docbook.org/guide/${VERSION}/ch-using.html#${PTOCID}`;
          let resp = `Error: <a class="showlink" href='${href}'>persistent ToC</a>`;
          if (uri.startsWith("file:")) {
            div.innerHTML = `${resp} is not accessible when the page is loaded using a <code>file:</code> URI.`;
          } else {
            div.innerHTML = `${resp} is not accessible: ${err}`;
          }
        });
    } else {
      processToC(event);
    }
  };

  const processToC = function(event) {
    let div = toc.querySelector("div");
    if (div.hasAttribute("db-prefix")) {
      patchToC(div, div.getAttribute("db-prefix"));
      div.removeAttribute("db-prefix");
      div.querySelectorAll("a").forEach(function (anchor) {
        anchor.onclick = function(event) {
          if (!tocPersist || !tocPersist.checked) {
            hideToC();
          }
          patchLink(event, anchor);
        };
      });
    }

    // Turn off any search markers that might have been set
    toc.querySelectorAll("li").forEach(function (li) {
      li.style.display = "list-item";
      const link = li.querySelector("a");
      if (link) {
        link.classList.remove("found");
      }
    });

    // Give the current click event a chance to settle?
    window.setTimeout(function () {
      const tocClose = toc.querySelector("header .close");
      curpress = document.onkeyup;
      tocClose.onclick = function (event) {
        hideToC(event);
      };
      document.onkeyup = function (event) {
        event = event || window.event;
        if (event.srcElement && event.srcElement.classList.contains("ptoc-search")) {
          // Don't navigate if the user is typing in the persistent toc search box
          return false;
        } else {
          let charCode = event.keyCode || event.which;
          if (charCode == SPACE || charCode == ESC) {
            hideToC(event);
            return false;
          }
          return true;
        }
      };

      let url = window.location.pathname;
      let hash = window.location.hash;
      if (window.location.search === "?toc") {
        // Remove ?toc from the URI so that if it's bookmarked,
        // the ToC reference isn't part of the bookmark.
        window.history.replaceState({}, document.title,
                                    window.location.origin
                                    + window.location.pathname
                                    + window.location.hash);
      }

      // Try path#hash
      let path = url.substring(1) + hash;
      let target = document.querySelector("nav.toc div a[rel-path='"+path+"']");
      if (target) {
        target.scrollIntoView();
      } else {
        // Try #hash
        target = document.querySelector("nav.toc div a[rel-path='"+hash+"']");
        if (target) {
          target.scrollIntoView();
        } else {
          // Try path
          target = document.querySelector("nav.toc div a[rel-path='"+url.substring(1)+"']");
          if (target) {
            target.scrollIntoView();
          } else {
            // ???
            console.log(`ToC scroll, no match: ${path}`);
          }
        }
      }

      if (!searchListener) {
        configureSearch();
        searchListener = true;
      }
    }, 400);

    return false;
  };

  const patchToC = function(elem, prefix) {
    // Injecting HTML is a little risky; try to mitigate that.
    // There should never *be* a script in there so...
    elem.querySelectorAll("script").forEach(script => {
      script.innerHTML = "";
      script.setAttribute("src", "");
    });

    elem.querySelectorAll("a").forEach(anchor => {
      anchor.setAttribute("rel-path", anchor.getAttribute("href"));
      anchor.setAttribute("href", prefix + anchor.getAttribute("href"));
    });

    return elem;
  };

  const hideToC = function(event) {
    document.onkeyup = curpress;
    toc.classList.add("slide");
    toc.style.width = "0px";
    toc.style["padding-left"] = "0";
    toc.style["padding-right"] = "0";
    toc.style["border-left"] = "none";

    if (event) {
      event.preventDefault();
    }

    const searchp = toc.querySelector(".ptoc-search");
    if (searchp) {
      const search = searchp.querySelector("input");
      if (search) {
        search.value = "";
      }
    }
    toc.querySelectorAll("li").forEach(function (li) {
      li.style.display = "list-item";
    });

    return false;
  };

  const tocPersistCheckbox = function(persist) {
    if (tocPersist != null) {
      return;
    }

    let ptoc = toc.querySelector("p.ptoc-search");
    let sbox = ptoc.querySelector("input.ptoc-search");
    if (sbox) {
      sbox.setAttribute("title", "Simple text search in ToC");
      let pcheck = document.createElement("input");
      pcheck.classList.add("persist");
      pcheck.setAttribute("type", "checkbox");
      pcheck.setAttribute("title", "Keep ToC open when following links");
      pcheck.checked = persist || (window.location.search === "?toc");
      ptoc.appendChild(pcheck);
    }

    tocPersist = toc.querySelector("p.ptoc-search .persist");
  };

  const patchLink = function(event, anchor) {
    if (!tocPersist || !tocPersist.checked) {
      return false;
    }

    let href = anchor.getAttribute("href");
    let pos = href.indexOf("#");

    if (pos === 0) {
      // If the anchor is a same-document reference, we don't
      // need to do any of this query string business.
      return false;
    }

    if (pos > 0) {
      href = href.substring(0, pos) + "?toc" + href.substring(pos);
    } else {
      href = href + "?toc";
    }

    event = event || window.event;
    if (event) {
      event.preventDefault();
    }
    window.location.href = href;
    return false;
  };

  const configureSearch = function() {
    const searchp = toc.querySelector(".ptoc-search");
    if (searchp == null) {
      return;
    }
    const search = searchp.querySelector("input");
    search.onkeyup = function (event) {
      event = event || window.event;
      if (event) {
        event.preventDefault();
      }
      let charCode = event.keyCode || event.which;
      if (charCode == ESC) {
        hideToC(event);
        return false;
      }

      const value = search.value.toLowerCase().trim();
      let restr = value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&').replace(" ", ".*");
      const regex = RegExp(restr);

      toc.querySelectorAll("li").forEach(function (li) {
        const link = li.querySelector("a");
        if (restr === "") {
          li.style.display = "list-item";
          if (link) {
            link.classList.remove("found");
          }
        } else {
          if (li.textContent.toLowerCase().match(regex)) {
            li.style.display = "list-item";
            if (link) {
              if (link.textContent.toLowerCase().match(regex)) {
                link.classList.add("found");
              } else {
                link.classList.remove("found");
              }
            }
          } else {
            li.style.display = "none";
          }
        }
      });

      return false;
    };
  };

  // Setting the border-left-style in CSS will put a thin border-colored
  // stripe down the right hand side of the window. Here we get the color
  // of that stripe and then remove it. We'll put it back when we
  // expand the ToC.
  borderLeftColor = window.getComputedStyle(toc)["border-left-color"];
  toc.style["border-left"] = "none";

  const tocOpenScript = document.querySelector("script.tocopen");
  const tocOpen = document.querySelector("nav.tocopen");
  tocOpen.innerHTML = tocOpenScript.innerHTML;
  tocOpen.onclick = showToC;

  const tocScript = document.querySelector("script.toc");
  toc.innerHTML = tocScript.innerHTML;

  tocOpen.style.display = "inline";

  /* N.B. these z-index changes "make sure" that the persistent ToC is visible
     in the nav bar, but they also interact with the z-index of the nav bar. If
     you're thinking of changing these, think of changing scss/media-screen.scss
     as well. */
  tocOpen.style.zIndex = 101;
  toc.style.zIndex = 102;

  if (window.location.search === "?toc") {
    showToC(null);
  } else {
    // If we're not going to jump immediately to the ToC,
    // add the slide class for aesthetics if the user clicks
    // on it.
    toc.classList.add("slide");
  }
})();
