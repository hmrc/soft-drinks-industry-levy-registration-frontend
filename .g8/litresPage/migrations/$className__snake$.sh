#!/bin/bash

echo ""
echo "Applying migration $className;format="snake"$"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /$litresUrl$                        controllers.HowMany$className$Controller.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /$litresUrl$                        controllers.HowMany$className$Controller.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /change-$litresUrl$                  controllers.HowMany$className$Controller.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /change-$litresUrl$                  controllers.HowMany$className$Controller.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en

echo "howMany$className$.title = $litresTitle$" >> ../conf/messages.en
echo "howMany$className$.heading = $litresHeading$" >> ../conf/messages.en
echo "howMany$className$.subtext = $litresSubText$" >> ../conf/messages.en
echo "$className;format="decap"$.lowband.litres.hidden = change amount of litres in lowband for $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.highband.litres.hidden = change amount of litres in highband for $className;format="decap"$" >> ../conf/messages.en

echo "Adding to Navigator"
awk '/class Navigator/ {\
    print;\
    print "";\
    print "  private def navigationFor$className$(userAnswers: UserAnswers, mode: Mode): Call = {";\
    print "    if (userAnswers.get(page = $className$Page).contains(true)) {";\
    print "      routes.HowMany$className$Controller.onPageLoad(mode)";\
    print "    } else if(mode == CheckMode){";\
    print "        routes.CheckYourAnswersController.onPageLoad";\
    print "    } else {";\
    print "        $nextPage$";\
    print "    }";\
    print "  }";\
    next }1' ../app/navigation/Navigator.scala > tmp && mv tmp ../app/navigation/Navigator.scala

awk '/private val normalRoutes/ {\
    print;\
    print "    case $className$Page => userAnswers => navigationFor$className$(userAnswers, NormalMode)";\
    print "    case HowMany$className$Page => userAnswers => $nextPage$";\
    next }1' ../app/navigation/Navigator.scala > tmp && mv tmp ../app/navigation/Navigator.scala

awk '/private val checkRouteMap/ {\
    print;\
    print "    case $className$Page => userAnswers => navigationFor$className$(userAnswers, CheckMode)";\
    next }1' ../app/navigation/Navigator.scala > tmp && mv tmp ../app/navigation/Navigator.scala

echo "Migration $className;format="snake"$ completed"
