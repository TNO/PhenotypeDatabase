<html>
    <head>
      <title>Generic Study Capture Framework</title>
      <meta name="layout" content="main" />
    </head>
    <body>
      Welcome to the first prototype of GSCF, version <b>${meta(name: 'app.version')}</b>. At this moment, there are ${studyCount} studies in the database.
      <p><n:isNotLoggedIn>
        For this version it is not required to login, but if you login as administrator ( admin / admiN123! ) or user ( user / useR123! ) you can test user functionality.
      </n:isNotLoggedIn></p>
  </body>
</html>