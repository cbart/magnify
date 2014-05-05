var getAuthors = function(callback, opt_rev) {
  $.getJSON("committers.json?rev=" + (opt_rev || ""), callback)
};

var renderAuthors = function() {
  getAuthors(function initialCreateStructure(authors) {
    var $authors = $('#authors');
    for (var i = 0; i < authors.length; i++) {
      var authorElem = createAuthorElem(authors[i]);
      $authors.append(authorElem)
    }
    setActiveAuthors();
  });
};

var createAuthorElem = function(authorMap) {
  var name = authorMap['name'];
  var id = getAuthorId(name);
  return $('<div/>').attr("id", "author_" + id).addClass("author").text(name);
};

var setActiveAuthors = function() {
  $(".author").removeClass("active");
  getAuthors(function setActive(authors) {
    for (var i = 0; i < authors.length; i++) {
      var name = authors[i]['name'];
      $("#author_" + getAuthorId(name)).addClass("active");
    }
  }, getActiveSha());
};

var getAuthorId = function(name) {
  return name.replace(new RegExp('[ <>@\.]', 'g'), "");;
};

$(function() {
  renderAuthors();
});
