package dev.gordeev.review.server.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PullRequest(
    val id: Long,
    val title: String,
    val fromRef: Reference,
    val toRef: Reference,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Reference(
    val id: String,
    val displayId: String
)

//{
//    "size" : 1,
//    "limit" : 10,
//    "isLastPage" : true,
//    "values" : [ {
//    "id" : 1,
//    "version" : 0,
//    "title" : "test",
//    "state" : "OPEN",
//    "open" : true,
//    "closed" : false,
//    "createdDate" : 1739617740025,
//    "updatedDate" : 1739617740025,
//    "fromRef" : {
//        "id" : "refs/heads/feature/f1",
//        "displayId" : "feature/f1",
//        "latestCommit" : "0e5f234479cf39b05b7dea5dc2ef1f795f2109b5",
//        "repository" : {
//        "slug" : "swtr-core",
//        "id" : 1,
//        "name" : "swtr-core",
//        "scmId" : "git",
//        "state" : "AVAILABLE",
//        "statusMessage" : "Available",
//        "forkable" : true,
//        "project" : {
//        "key" : "SWTR",
//        "id" : 1,
//        "name" : "SWTR",
//        "public" : false,
//        "type" : "NORMAL",
//        "links" : {
//        "self" : [ {
//        "href" : "http://localhost:7990/projects/SWTR"
//    } ]
//    }
//    },
//        "public" : true,
//        "links" : {
//        "clone" : [ {
//        "href" : "ssh://git@localhost:7999/swtr/swtr-core.git",
//        "name" : "ssh"
//    }, {
//        "href" : "http://localhost:7990/scm/swtr/swtr-core.git",
//        "name" : "http"
//    } ],
//        "self" : [ {
//        "href" : "http://localhost:7990/projects/SWTR/repos/swtr-core/browse"
//    } ]
//    }
//    }
//    },
//    "toRef" : {
//        "id" : "refs/heads/main",
//        "displayId" : "main",
//        "latestCommit" : "10d79abbfa5db5a8d365024fca8827363e860994",
//        "repository" : {
//        "slug" : "swtr-core",
//        "id" : 1,
//        "name" : "swtr-core",
//        "scmId" : "git",
//        "state" : "AVAILABLE",
//        "statusMessage" : "Available",
//        "forkable" : true,
//        "project" : {
//        "key" : "SWTR",
//        "id" : 1,
//        "name" : "SWTR",
//        "public" : false,
//        "type" : "NORMAL",
//        "links" : {
//        "self" : [ {
//        "href" : "http://localhost:7990/projects/SWTR"
//    } ]
//    }
//    },
//        "public" : true,
//        "links" : {
//        "clone" : [ {
//        "href" : "ssh://git@localhost:7999/swtr/swtr-core.git",
//        "name" : "ssh"
//    }, {
//        "href" : "http://localhost:7990/scm/swtr/swtr-core.git",
//        "name" : "http"
//    } ],
//        "self" : [ {
//        "href" : "http://localhost:7990/projects/SWTR/repos/swtr-core/browse"
//    } ]
//    }
//    }
//    },
//    "locked" : false,
//    "author" : {
//        "user" : {
//        "name" : "superstep",
//        "emailAddress" : "superstep007@gmail.com",
//        "id" : 1,
//        "displayName" : "Gordeev Egor",
//        "active" : true,
//        "slug" : "superstep",
//        "type" : "NORMAL",
//        "links" : {
//        "self" : [ {
//        "href" : "http://localhost:7990/users/superstep"
//    } ]
//    }
//    },
//        "role" : "AUTHOR",
//        "approved" : false,
//        "status" : "UNAPPROVED"
//    },
//    "reviewers" : [ ],
//    "participants" : [ ],
//    "properties" : {
//        "mergeResult" : {
//        "outcome" : "CLEAN",
//        "current" : true
//    },
//        "resolvedTaskCount" : 0,
//        "commentCount" : 1,
//        "openTaskCount" : 0
//    },
//    "links" : {
//        "self" : [ {
//        "href" : "http://localhost:7990/projects/SWTR/repos/swtr-core/pull-requests/1"
//    } ]
//    }
//} ],
//    "start" : 0
//}
