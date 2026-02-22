/* DocBook xslTNG version 2.5.0
 *
 * See https://xsltng.docbook.org/
 *
 * This is presentation.js providing support for presentations.
 * A presentation is a single-file HTML rendering of a DocBook
 * document with custom CSS and custom JavaScript to give a 
 * "one slide per page" navigable view.
 *
 * There are a few things going on here.
 *
 * 1. The HTML is extensively modified. It is turned into a main
 *    element containing a flat list of article elements, one per page.
 *    All of the intervening article/div/section wrappers are discarded.
 *    This simplifies CSS for each page.
 * 2. N/→, P/←, U, D, and H/Home navigate to the next, previous, "up",
 *    "down" and home pages.
 * 3. If the HTML page contains a meta element with the name
 *    "localStorage.key", the value of that element is used as a key
 *    in the browser's localStorage API to track the Window location
 *    and revealed items.
 * 4. The S key can be used to switch between a normal view and a
 *    speaker notes view. Speaker notes are (block) elements identified
 *    with role='speaker-notes' in the source XML.
 * 5. Progressive reveal of items (including transient reveals) are
 *    supported. Mark items to be revealed with role='reveal'. If you
 *    put put that role on a list, it applies to all the items except
 *    the first. If you want to make an item always appear, mark it
 *    role='noreveal'. If you want an item to be transitory (appear
 *    until something after it is revealed), mark it
 *    role='transitory'.
 *
 * You can avoid flicker if you configure CSS to hide the things that
 * should be hidden by default. This JavaScript will handle that case,
 * even when progressiveReveal is disabled.
 */

(function() {
  const KEY_N = 78;
  const KEY_RIGHT = 39;

  const KEY_P = 80;
  const KEY_LEFT = 37;

  const KEY_U = 85;
  const KEY_D = 68;

  const KEY_H = 72;
  const KEY_HOME = 36;

  const KEY_A = 65;
  const KEY_R = 82;
  const KEY_S = 83;

  const KEY_SPACE = 32;

  const KEY_SHIFT = 16;
  const KEY_QUESTION = 191;
  const KEY_F1 = 112;
  const KEY_ESC = 27;

  const body = document.querySelector("body");
  const main = body.querySelector("main");
  const navtop = body.querySelector("nav.top");
  const navbot = body.querySelector("nav.bottom");

  const navdiv = document.createElement("div");
  navdiv.setAttribute("class", "foilnav");

  const nodemap = {};

  let footnotes = null;
  let footnoteids = [];
  let helpdiv = null;
  let notesView = false;

  let homeicon = "⏹";  // "⌂";
  let nexticon = "⏵"; // "⮞";
  let previcon = "⏴"; // "⮜";
  let upicon = "⏶";   // "⮝";
  let downicon = "⏷"; // "⮟";
  let follicon = "⏭";
  let precicon = "⏮";

  let foil = null;
  let current = null;

  const getMeta = function(name) {
    const meta = document.querySelector(`head meta[name='${name}']`);
    const key = meta && meta.getAttribute("content");
    return key ? key : null;
  };

  const windowStorage = (function() {
    const lskey = getMeta("localStorage.key");
    const locationKey = lskey ? `${lskey}_loc` : null;
    const revealedKey = lskey ? `${lskey}_reveal` : null;

    const storeLocation = function(loc) {
      window.localStorage.setItem(locationKey, window.location);
    };

    const updateLocation = function() {
      window.localStorage.setItem(locationKey, window.location.href);
    };

    const storeRevealed = function(revealed) {
      window.localStorage.setItem(revealedKey, revealed.join(","));
    };

    const storageChange = function(changes, areaName) {
      if (changes.key === locationKey) {
        if (changes.newValue !== window.location.href) {
          window.location.href = changes.newValue;
        }
      }

      if (changes.key === revealedKey) {
        let newreveal = [];
        if (changes.newValue) {
          newreveal = changes.newValue.split(",");
        }

        document.querySelectorAll(".reveal, .revealed").forEach(item => {
          if (newreveal.indexOf(item.id) < 0) {
            item.classList.replace("revealed", "reveal");
          } else {
            item.classList.replace("reveal", "revealed");
          }
        });
        
        reveal.checkDocument();
      }
    };

    const nullFunction = function(arg) { };

    return {
      "storeLocation": locationKey ? storeLocation : nullFunction,
      "updateLocation": locationKey ? updateLocation : nullFunction,
      "storeRevealed": revealedKey ? storeRevealed : nullFunction,
      "storageChange": locationKey ? storageChange : function(changes, areaName) { }
    };
  })();

  const reveal = (function() {
    const startRevealed = ["true", "1", "yes"].indexOf(getMeta("start-revealed")) >= 0;
    let threeDots = null;
    let currentlyRevealed = [];
    let prid = "pr_id";
    let prnum = 0;

    const configure = function(elem) {
      if (elem.tagName === "UL" && elem.classList.contains("toc")) {
        // nop; don't attempt to do reveal processing on tables of contents
      } else if (elem.tagName === "UL" || elem.tagName === "OL") {
        // Lists are special; hide all but the first item by default
        configureList(elem);
      } else if (elem.tagName == "DL") {
        configureDL(elem);
      } else if (elem.tagName === "SCRIPT") {
        // don't look in script elements
      } else if (elem.classList.contains("speaker-notes")) {
        // don't do reveals in speaker-notes
      } else {
        if (elem.classList.contains("reveal")) {
          if (!elem.id) {
            elem.id = `${prid}_${++prnum}`;
          }
          if (startRevealed) {
            elem.classList.replace("reveal", "revealed");
          }
        }
        elem.querySelectorAll(":scope > *").forEach(child => {
          configure(child);
        });
      }
    };

    const configureList = function(list) {
      const isprogressive = list.classList.contains("reveal");
      if (isprogressive) {
        list.classList.remove("reveal"); // children effectively inherit this

        let itemnum = 0;
        list.querySelectorAll(":scope > li").forEach(item => {
          itemnum++;

          if (item.classList.contains("noreveal")) {
            // ignore
          } else {
            if (itemnum > 1) {
              item.classList.add("reveal");
              if (!item.id) {
                item.id = `${prid}_${++prnum}`;
              }
              if (startRevealed) {
                item.classList.replace("reveal", "revealed");
              }
            }
          }

          // Now process the descendants of the list items
          item.querySelectorAll(":scope > *").forEach(child => {
            configure(child);
          });
        });
      } else {
        // If the list doesn't have a reveal, check its children
        list.querySelectorAll(":scope > *").forEach(item => {
          configure(item);
        });
      }
    };

    const configureDL = function(list) {
      const isprogressive = list.classList.contains("reveal");
      if (isprogressive) {
        list.classList.remove("reveal"); // children effectively inherit this

        let itemnum = 0;
        list.querySelectorAll(":scope > div").forEach(item => {
          itemnum++;

          if (item.classList.contains("noreveal")) {
            // ignore it
          } else {
            if (itemnum > 1) {
              item.classList.add("reveal");
              if (!item.id) {
                item.id = `${prid}_${++prnum}`;
              }
              if (startRevealed) {
                item.classList.replace("reveal", "revealed");
              }
            }
          }

          // Now process the descendants of the list items
          item.querySelectorAll(":scope > *").forEach(child => {
            configure(child);
          });
        });
      } else {
        // If the list doesn't have a reveal, check its children
        list.querySelectorAll(":scope > *").forEach(item => {
          configure(item);
        });
      }
    };

    const moreToReveal = function() {
      let found = false;
      current.node.querySelectorAll(".reveal").forEach(item => {
        if (!item.classList.contains("transitory")) {
          found = true;
        }
      });
      return found;
    };

    const show = function(node) {
      if (node.classList.contains("reveal")) {
        node.classList.replace("reveal", "revealed");
        node.scrollIntoView();
        if (currentlyRevealed.indexOf(node.id) < 0) {
          currentlyRevealed.push(node.id);
          windowStorage.storeRevealed(currentlyRevealed);
        }
        displayThreeDots();
      }
    };

    const hide = function(node) {
      if (!node.classList.contains("reveal")) {
        if (node.classList.contains("revealed")) {
          node.classList.replace("revealed", "reveal");
        } else {
          node.classList.add("reveal");
        }
        let idx = currentlyRevealed.indexOf(node.id);
        if (idx > -1) {
          currentlyRevealed.splice(idx, 1);
          windowStorage.storeRevealed(currentlyRevealed);
        }
        displayThreeDots();
      }
    };

    const resetTransitory = function(node) {
      // This can happen for transitory items initially marked noreveal
      if (node.classList.contains("noreveal") && node.classList.contains("reveal")) {
        node.classList.remove("reveal");
      }
    };

    const revealNext = function() {
      // Find the next item to reveal by working from the last back towards
      // the front. This allows us to re-hide transitory items without
      // immediately re-revealing them on the next call.
      let items = [];
      current.node.querySelectorAll(".reveal,.revealed,.transitory").forEach(item => {
        items.push(item);
      });
      let ridx = -1;
      for (let idx = items.length-1; idx >= 0; idx--) {
        if (items[idx].classList.contains("reveal")) {
          ridx = idx;
        }
        if (items[idx].classList.contains("revealed")) {
          break;
        }
      }

      // If there are any transitory and revealed items "before us",
      // hide them again. Ancestors don't count...but we're starting
      // at the transitory item so that amounts to "not in our descendants".
      if (ridx >= 0) {
        const selected = items[ridx];
        show(selected);
        for (ridx--; ridx >= 0; ridx--) {
          if (items[ridx].classList.contains("transitory")) {
            let ancestor = false;
            items[ridx].querySelectorAll("*").forEach(child => {
              ancestor = ancestor || child.isSameNode(selected);
            });
            if (!ancestor) {
              hide(items[ridx]);
            }
          }
        }
        return true;
      }

      return false;
    };

    const revealAll = function() {
      document.querySelectorAll(".reveal").forEach(item => {
        show(item);
      });
      document.querySelectorAll(".transitory").forEach(item => {
        hide(item);
      });

      displayThreeDots();
    };

    const reset = function() {
      // Put everything back the way it was
      document.querySelectorAll(".transitory").forEach(item => {
        resetTransitory(item);
      });

      document.querySelectorAll(".revealed").forEach(elem => {
        hide(elem);
      });

      displayThreeDots();
    };

    const toggle = function() {
      // If there are any transitory items marked both reveal and noreveal,
      // remove the "reveal" class.
      current.node.querySelectorAll(".transitory").forEach(elem => {
        resetTransitory(elem);
      });

      // If there are any (non-transitory) hidden items, reveal
      // all the items on this page. Otherwise, hide them.
      let found = false;
      current.node.querySelectorAll(".reveal").forEach(elem => {
        if (!elem.classList.contains("transitory")) {
          found = true;
        }
      });

      if (found) {
        current.node.querySelectorAll(".reveal").forEach(elem => {
          show(elem);
        });
        current.node.querySelectorAll(".transitory").forEach(elem => {
          hide(elem);
        });
      } else {
        current.node.querySelectorAll(".revealed").forEach(elem => {
          hide(elem);
        });
      }

      displayThreeDots();
    };

    const checkDocument = function() {
      currentlyRevealed = [];
      document.querySelectorAll(".revealed").forEach(item => {
        currentlyRevealed.push(item.id);
      });
      displayThreeDots();
    };

    const displayThreeDots = function() {
      if (!threeDots) {
        threeDots = document.createElement("div");
        threeDots.classList.add("threedots");
        threeDots.innerHTML = "⋮";
        navbot.appendChild(threeDots);
      }

      if (moreToReveal()) {
        threeDots.style.display = "block";
      } else {
        threeDots.style.display = "none";
      }
    };

    return {
      "configure": function() { configure(body); },
      "more": moreToReveal,
      "next": revealNext,
      "reset": reset,
      "toggle": toggle,
      "all": revealAll,
      "show": displayThreeDots,
      "checkDocument": checkDocument
    };
  })();

  // ============================================================

  const keyboard = function(event) {
    event = event || window.event;
    let keyCode = event.keyCode || event.which;

    if (helpdiv && helpdiv.style.display == "block") {
      helpdiv.style.display = "none";
      return false;
    }

    if (event.srcElement && event.srcElement.classList.contains("ptoc-search")) {
      // Don't navigate if the user is typing in the persistent toc search box
      return true;
    }

    switch (keyCode) {
    case KEY_N:
    case KEY_RIGHT:
      if (event.shiftKey) {
        nav("following");
      } else {
        if (!reveal.next()) {
          nav("next");
        }
      }
      break;
    case KEY_P:
    case KEY_LEFT:
      nav(event.shiftKey ? "preceding" : "prev");
      break;
    case KEY_U:
      nav("up");
      break;
    case KEY_SPACE:
      reveal.next();
      break;
    case KEY_D:
      nav("down");
      break;
    case KEY_H:
    case KEY_HOME:
      navTo(foil);
      break;
    case KEY_A:
      reveal.all();
      break;
    case KEY_R:
      if (event.shiftKey) {
        reveal.reset();
      } else {
        reveal.toggle();
      }
      break;
    case KEY_S:
      viewNotes(!notesView);
      break;
    case KEY_QUESTION:
    case KEY_F1:
      help();
      break;
    default:
      break;
    }

    return false;
  };

  const nav = function (direction) {
    if (current[direction]) {
      navTo(current[direction]);
    }
  };

  const navHash = function(target) {
    if (target && target.node) {
      return "#" + target.node.getAttribute("tumble-id").substring(2);
    } else {
      return "#1";
    }
  };

  const navLink = function(target, icon) {
    if (target) {
      return `<span class='live'><a href="${navHash(target)}">${icon}</a></span>`;
    }
    return `<span class='dead'>${icon}</span>`;
  };

  const navTo = function(destination) {
    current.node.classList.remove("show");
    current = destination;
    current.node.classList.add("show");
    window.location.hash = navHash(current);
    windowStorage.storeLocation(window.location);

    current.node.scrollIntoView();

    // Let's be lazy and do this with strings
    let inner;
    if (current.up) {
      inner = `<span class='live'><a href="#0">${homeicon}</a></span>`;
    } else {
      inner = `<span class='dead'>${homeicon}</span>`;
    }

    inner += navLink(current.up, upicon);
    inner += navLink(current.down, downicon);
    inner += navLink(current.prev, previcon);
    inner += navLink(current.next, nexticon);
    //inner += navLink(current.preceding, precicon);
    //inner += navLink(current.following, follicon);

    navdiv.innerHTML = inner;

    viewNotes(notesView);
    reveal.show();
  };

  const initialSlide = function(event) {
    event.preventDefault();

    let startFoil = "R.1";
    if (window.location.hash.startsWith("#")) {
      startFoil = "R." + window.location.hash.substring(1);
    }
    
    if (!(startFoil in nodemap)) {
      startFoil = "R.1";
    }

    navTo(nodemap[startFoil]);
  };

  const viewNotes = function(view) {
    let div = current.node;
    let inset = div.querySelector(".inset-wrapper");
    if (!inset) {
      let content = document.createElement("div");
      content.classList.add("inset-wrapper");

      let cbody = document.createElement("div");
      cbody.classList.add("inset-body");

      let notes = document.createElement("div");
      notes.classList.add("speaker-notes-wrapper");

      let children = [];
      for (let idx = 0; idx < div.children.length; idx++) {
        children.push(div.children[idx]);
      }
      children.forEach(child => {
        cbody.appendChild(child.parentNode.removeChild(child));
      });

      children = [];
      cbody.querySelectorAll(".speaker-notes").forEach(child => {
        children.push(child);
      });

      children.forEach(child => {
        notes.appendChild(child.parentNode.removeChild(child));
      });

      // What about footnotes? Copy the relevant footnotes onto each page.
      // Remove the bidirection links between footnote marks because they
      // confuse the presentation mode navigation.
      let pagefn = [];
      cbody.querySelectorAll("a[href]").forEach(anchor => {
        let href = anchor.getAttribute("href");
        if (href.startsWith("#") && footnoteids.indexOf(href.substring(1)) >= 0) {
          pagefn.push(href.substring(1));
          anchor.removeAttribute("href");
        }
      });
      if (pagefn.length > 0) {
        let footer = document.createElement("footer");
        let fndiv = document.createElement("div");
        fndiv.classList.add("footnotes");
        fndiv.appendChild(document.createElement("hr"));
        footnotes.querySelectorAll("div.footnote[id]").forEach(fn => {
          if (pagefn.indexOf(fn.id) >= 0) {
            let newfn = fn.cloneNode(true);
            newfn.querySelectorAll("a[href]").forEach(anchor => {
              if (anchor.getAttribute("href").startsWith("#")) {
                anchor.removeAttribute("href");
              }
            });
            fndiv.appendChild(newfn);
          }
        });
        footer.appendChild(fndiv);
        content.appendChild(footer);
      }

      div.appendChild(content);
      content.prepend(cbody);
      div.appendChild(notes);
    }

    let content = current.node.querySelector(".inset-wrapper");
    let notes = current.node.querySelector(".speaker-notes-wrapper");
    if (view) {
      content.classList.add("inset");
      notes.style.display = "block";
    } else {
      content.classList.remove("inset");
      notes.style.display = "none";
    }

    notesView = view;
  };

  // ============================================================

  const tidyMarkup = function(root, depth=0) {
    const page = document.createElement("article");
    page.setAttribute("class", "leaf");

    const unwrapped = [];
    // Wrap up all the non-article, non-section children.
    root.querySelectorAll(":scope > *").forEach(child => {
      let ischunk = child.tagName == "ARTICLE" || child.tagName == "SECTION";
      ischunk = ischunk && !(child.classList.contains("partintro"));

      if (ischunk) {
        tidyMarkup(child, depth+1);
      } else {
        unwrapped.push(child);
      }
    });

    unwrapped.forEach(child => {
      child.parentNode.removeChild(child);
      page.appendChild(child);
    });

    page.classList.add("depth_" + depth);
    root.prepend(page);
  };

  const buildNavigation = function(root, depth=1, up=null, tumble="R") {
    let selector = `.depth_${depth}`;
    if (depth == 1) {
      // The book titlepage is special
      selector = ".depth_0, .depth_1";
    }
    let first = null;
    let siblings = null;
    let pos = 0;
    root.querySelectorAll(selector).forEach(node => {
      pos++;
      const tumbleId = `${tumble}.${pos}`;
      node.setAttribute("tumble-id", tumbleId);

      let next = {
        "node": node,
      };
      nodemap[tumbleId] = next;

      if (up) {
        next.up = up;
      } else {
        up = next;
      }

      if (siblings) {
        siblings.following = next;
        next.preceding = siblings;
      } else {
        first = next;
      }

      let down = null;
      if (!node.classList.contains("depth_0")) {
        down = buildNavigation(node.parentNode, depth+1, next, tumbleId);
      }
      if (down && down.node) {
        next.down = down;
      }

      siblings = next;
    });

    return first;
  };

  const sequentialNavigation = function(book) {
    let prev = null;
    book.querySelectorAll(".leaf").forEach(leaf => {
      const node = nodemap[leaf.getAttribute("tumble-id")];
      if (prev) {
        prev.next = node;
        node.prev = prev;
      }
      prev = node;
    });
  };

  const patchLinks = function(book) {
    book.querySelectorAll("a[href]").forEach(link => {
      let href = link.getAttribute("href");
      if (href.startsWith("#")) {
        const node = document.querySelector(href);
        if (node) {
          const leaf = node.querySelector(".leaf");
          if (leaf) {
            link.setAttribute("href", `#${leaf.getAttribute("tumble-id").substring(2)}`);
          }
        }
      }
    });
  };

  const stripWrappers = function(book) {
    let page = foil;
    let lastInsert = null;
    while (page) {
      const leaf = page.node.parentNode.removeChild(page.node);
      leaf.classList.remove("leaf");
      if (lastInsert) {
        main.insertBefore(leaf, lastInsert.nextSibling);
      } else {
        main.prepend(leaf);
      }
      lastInsert = leaf;
      page = page.next;
    }
    book.parentNode.removeChild(book);
  };

  // ============================================================

  const help = function() {
    let count = 0;
    current.node.querySelectorAll(".reveal").forEach(function(item) {
      count += 1;
    });
    console.log(count, "items to be revealed on this page:");
    console.log(current);

    if (!helpdiv) {
      // Inlining this in JavaScript code is just such a hack...
      helpdiv = document.createElement("div");
      helpdiv.classList.add(`popup-annotation-wrapper`);

      let innerdiv = document.createElement("div");
      innerdiv.classList.add("popup-annotation-body");
      innerdiv.style.width = "90%";
      innerdiv.style.marginTop = "5%";
      innerdiv.style.maxHeight = "80%";
      innerdiv.style.height = "80%";

      let headerdiv = document.createElement("div");
      headerdiv.classList.add("popup-annotation-header");
      let span = document.createElement("span");
      span.innerHTML = "Help";
      headerdiv.appendChild(span);
      span = document.createElement("span");
      span.innerHTML = "(Esc to close)";
      span.style.float = "right";
      headerdiv.appendChild(span);

      let contentdiv = document.createElement("div");
      contentdiv.classList.add("popup-annotation-content");
      contentdiv.style.fontSize = "16pt";
      contentdiv.innerHTML = "<div>"
        + "<p>Presentation mode displays a document as a set of individual pages. "
        + "Navigation is performed by following links, "
        + "clicking the icons in the lower-right corner of the "
        + "page, or with the keyboard.</p>"
        + ""
        + "<table>"
        + "<thead>"
        + "<tr><th>Key</th><th>Navigation</th></tr>"
        + "</thead>"
        + "<tbody>"
        + "<tr><td>Space</td><td>Reveal next item</td></tr>"
        + "<tr><td>N or →</td><td>Reveal next item, or next sequential page</td></tr>"
        + "<tr><td>Shift+N or Shift + →</td><td>Next sibling page</td></tr>"
        + "<tr><td>P or ←</td><td>Previous sequential page</td></tr>"
        + "<tr><td>Shift+P or Shift + ←</td><td>Previous sibling page</td></tr>"
        + "<tr><td>U</td><td>Nearest ancestor page</td></tr>"
        + "<tr><td>D</td><td>First descendant page</td></tr>"
        + "<tr><td>H or Home</td><td>Return to title page</td></tr>"
        + "<tr><td>A</td><td>Reveal all hidden items</td></tr>"
        + "<tr><td>R</td><td>Toggle hidden items on the current page</td></tr>"
        + "<tr><td>Shift+R</td><td>Reset all hidden items in the document</td></tr>"
        + "<tr><td>S</td><td>Toggle notes view</td></tr>"
        + "<tr><td>? or F1</td><td>Display this help screen</td></tr>"
        + "</tbody>"
        + "</table>";

      if (window.location.href.startsWith("http://localhost/")
          || window.location.href.startsWith("http://localhost:")
          || window.location.href.startsWith("http://127.0.0.1/")
          || window.location.href.startsWith("http://127.0.0.1:")
          || window.location.href.startsWith("https:")) {
        contentdiv.innerHTML += ""
          + "<p>Presentation mode can use the local "
          + "storage API to keep several browser windows in sync, for example to display "
          + "notes in one window while projecting another.</p>";
      } else {
        contentdiv.innerHTML += ""
          + "<p>If loaded from localhost or via https:, presentation mode can use the local "
          + "storage API to keep several browser windows in sync.</p>";
      }
      contentdiv.innerHTML += "</div>";

      innerdiv.appendChild(headerdiv);
      innerdiv.appendChild(contentdiv);
      helpdiv.appendChild(innerdiv);
      body.appendChild(helpdiv);
    }

    helpdiv.style.display = "block";
  };

  // ============================================================

  navdiv.innerHTML = "";
  navbot.appendChild(navdiv);

  footnotes = document.querySelector("footer div.footnotes");
  if (footnotes) {
    // Excise them from the document
    footnotes = footnotes.parentNode.removeChild(footnotes);
    footnotes.querySelectorAll("div.footnote[id]").forEach(fn => {
      footnoteids.push(fn.id);
    });
  }

  const book = main.querySelector("article");
  tidyMarkup(book);
  foil = buildNavigation(book);
  sequentialNavigation(book);
  patchLinks(book);
  stripWrappers(book);

  // The root title page is special, "down" == "next"
  foil.down = foil.next;

  current = foil;
  if (window.location.hash.startsWith("#")) {
    let startFoil = "R." + window.location.hash.substring(1);
    if (startFoil in nodemap) {
      current = nodemap[startFoil];
    }
  }

  reveal.configure();
  navTo(current);

  windowStorage.updateLocation();

  window.addEventListener("keyup", keyboard);
  window.addEventListener("hashchange", initialSlide);
  window.addEventListener("storage", windowStorage.storageChange);
})();
