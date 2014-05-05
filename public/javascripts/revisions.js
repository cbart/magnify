var getRevisions = function(callback) {
  $.getJSON("revisions.json", callback)
};

var renderRevisions = function() {
  getRevisions(function initialCreateRevStructure(revisions) {
    var $revisions = $('#revisions');
    for (var i = 0; i < revisions.length; i++) {
      var classes = [];
      if (i == revisions.length - 1) {
        classes.push("active");
      }
      var revElem = createRevElement(revisions[i], classes);
      $revisions.append(revElem)
    }
  });
};

var createRevElement = function(revMap, classes) {
  var sha = revMap['id'];
  var author = revMap['author'];
  var committer = revMap['committer'];
  var desc = revMap['description'];
  var time = revMap['time'];
  var shaElem = $('<span/>').addClass("sha").text(sha);
  var authorElem = $('<span/>').addClass("author").text(author);
  var committerElem = $('<span/>').addClass("committer").text(committer);
  var descElem = $('<span/>').addClass("desc").text(desc);
  var timeElem = $('<span/>').addClass("time").text(time);
  var detailsElem = $('<div/>').addClass("details")
      .append(shaElem).append(" ").append(authorElem).append(" @ ").append(timeElem).append($('<br/>'))
      .append("commited by: ").append(committerElem).append($('<br/>'))
      .append(descElem).hide();
  var elem = $('<div/>').attr("id", "rev_" + sha).addClass("revision")
      .text(sha + " " + desc + " # " + author)
      .append(detailsElem);
  for (var i = 0; i < classes.length; i++) {
    elem = elem.addClass(classes[i]);
  }
  return elem
};

var getActiveSha = function() {
  var activeElemId = $('.revision.active').attr("id");
  return (activeElemId && activeElemId.split("_")[1]) || "";
};

$(function() {
  renderRevisions();
});
