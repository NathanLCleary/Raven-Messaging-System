# -*- coding: utf-8 -*-

# This sample demonstrates handling intents from an Alexa skill using the Alexa Skills Kit SDK for Python.
# Please visit https://alexa.design/cookbook for additional examples on implementing slots, dialog management,
# session persistence, api calls, and more.
# This sample is built using the handler classes approach in skill builder.
import logging
import ask_sdk_core.utils as ask_utils
import requests

from ask_sdk_core.skill_builder import SkillBuilder
from ask_sdk_core.dispatch_components import AbstractRequestHandler
from ask_sdk_core.dispatch_components import AbstractExceptionHandler
from ask_sdk_core.handler_input import HandlerInput

from ask_sdk_model import Response

import paho.mqtt.client as paho
from paho import mqtt

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

messageContent = ''
messageName = ''


client = paho.Client(client_id="", userdata=None, protocol=paho.MQTTv5)

# enable TLS for secure connection
client.tls_set(tls_version=mqtt.client.ssl.PROTOCOL_TLS)
# set username and password
client.username_pw_set("", "")
# connect to HiveMQ Cloud on port 8883 (default for MQTT)
client.connect("1b4905309ef3436aafb2b54ffc32b2e9.s2.eu.hivemq.cloud", 8883)

class LaunchRequestHandler(AbstractRequestHandler):
    """Handler for Skill Launch."""
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return ask_utils.is_request_type("LaunchRequest")(handler_input)

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response
        speak_output = "Hello and welcome to Raven Messaging, Who would you like to message?"

        return (
            handler_input.response_builder
                .speak(speak_output)
                .ask(speak_output)
                .response
        )

class HelloWorldIntentHandler(AbstractRequestHandler):
    """Handler for Hello World Intent."""
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return ask_utils.is_intent_name("HelloWorldIntent")(handler_input)

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response
        speak_output = "Hello World!"

        return (
            handler_input.response_builder
                .speak(speak_output)
                # .ask("add a reprompt if you want to keep the session open for the user to respond")
                .response
        )

class MessageWhoIntentHandler(AbstractRequestHandler):
    """Handler for Hello World Intent."""
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return ask_utils.is_intent_name("MessageWhoIntent")(handler_input)

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response
        
        slots = handler_input.request_envelope.request.intent.slots
        
        
        messageName = slots['name'].value 
        # https://ravendatabaseapi.azurewebsites.net/api/signin/%7Busername%7D/%7Bpassword%7D?code=DsaxwzqBzqGCV3m_PvlrvKrh36hX9yo02Zwrto-dvrWZAzFullV_Ow%3D%3D
        x = requests.get('https://ravendatabaseapi.azurewebsites.net/api/signin/'
                            +'/Alexa-' + messageName
                            +'/testPassword'
                            + '?code=DsaxwzqBzqGCV3m_PvlrvKrh36hX9yo02Zwrto-dvrWZAzFullV_Ow%3D%3D')
                            
        x = x.text
        y = x[0:3]
                            
        if(y == "400"):
            return (
            handler_input.response_builder
                .speak(speak_output)
                .set_should_end_session(False)
                .ask("The user " + messageName +  " doesnt exist. Please try again.")
                .response
            )
        
        session_attr = handler_input.attributes_manager.session_attributes
        # Get the slot value from the request and add it to the session 
        # attributes dictionary. Because of the dialog model and dialog 
        # delegation, this code only ever runs when the favoriteColor slot 
        # contains a value, so a null check is not necessary.
        
        session_attr["messageRecipient"] = messageName
        
        speak_output = "Okay sending a message to " + messageName + ". What is the message?"
        
        return (
            handler_input.response_builder
                .speak(speak_output)
                .set_should_end_session(False)
                .ask("What is the message?")
                .response
        )


class MessageContentIntentHandler(AbstractRequestHandler):
    """Handler for Hello World Intent."""
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return ask_utils.is_intent_name("MessageContentIntent")(handler_input)

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response
        
        session_attr = handler_input.attributes_manager.session_attributes
        # The user could invoke this intent before they set their favorite 
        # color, so check for the session attribute first.
        
        if "messageRecipient" in session_attr:
            messageName = session_attr["messageRecipient"]
            
            slots = handler_input.request_envelope.request.intent.slots
            messageContent = slots['message'].value
            messageName = messageName.lower()
            
            requests.get('https://ravendatabaseapi.azurewebsites.net/api/GetPost'
                            +'/Alexa-' + messageName
                            +'/' + messageContent
                            + '?code=MKFwGhxYvbQvqsJgV-lcuDMoco2v4xbG2ecf6_3QXCpQAzFuSjDRTw==')
                            
            message_info = client.publish("alexa/" + messageName, payload=messageContent, qos=1)     
            
            #message_info.wait_for_publish()
            
            client.disconnect
            
            speak_output = "Okay, I have sent the message to " + messageName + ". Goodbye."
            
            return (
            handler_input.response_builder
                .speak(speak_output)
                .set_should_end_session(True)
                .response
            )
        
        if "messageRecipient" not in session_attr:
            # The user must have invoked this intent before they set their color. 
            # Trigger the FavoriteColorIntent and ask the user to fill in the 
            # favoriteColor slot. Note that the skill must have a *dialog model* 
            # to use the ElicitSlot Directive.
            
            return handler_input.response_builder.speak(
                "To send a message, I need to know who to send it to.").ask(
                "Who would you like to send it to?.").add_directive(
                directive=ElicitSlotDirective(
                    updated_intent=Intent(
                    name="MessageWhoIntent"), 
                    slot_to_elicit="name")).response
        
        #speak_output = "Fallback output."

        #return (
        #    handler_input.response_builder
        #        .speak(speak_output)
        #        .set_should_end_session(True)
        #        #.ask("add a reprompt if you want to keep the session open for the user to respond")
       #         .response
       #)

class HelpIntentHandler(AbstractRequestHandler):
    """Handler for Help Intent."""
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return ask_utils.is_intent_name("AMAZON.HelpIntent")(handler_input)

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response
        speak_output = ("This is Raven Messaging. I can send a messages to someone with the Raven App. Please tell me who to message by saying, Tell and the username of the person. For example, Tell Mary."
        + "Once I have the name tell me the message. Please start the message with Message Content. I will then send the message to the recipient.")

        return (
            handler_input.response_builder
                .speak(speak_output)
                .ask(speak_output)
                .response
        )


class CancelOrStopIntentHandler(AbstractRequestHandler):
    """Single handler for Cancel and Stop Intent."""
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return (ask_utils.is_intent_name("AMAZON.CancelIntent")(handler_input) or
                ask_utils.is_intent_name("AMAZON.StopIntent")(handler_input))

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response
        speak_output = "Goodbye!"

        return (
            handler_input.response_builder
                .speak(speak_output)
                .response
        )

class FallbackIntentHandler(AbstractRequestHandler):
    """Single handler for Fallback Intent."""
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return ask_utils.is_intent_name("AMAZON.FallbackIntent")(handler_input)

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response
        logger.info("In FallbackIntentHandler")
        speech = "Hmm, I'm not sure. You can say Hello or Help. What would you like to do?"
        reprompt = "I didn't catch that. What can I help you with?"

        return handler_input.response_builder.speak(speech).ask(reprompt).response

class SessionEndedRequestHandler(AbstractRequestHandler):
    """Handler for Session End."""
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return ask_utils.is_request_type("SessionEndedRequest")(handler_input)

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response

        # Any cleanup logic goes here.

        return handler_input.response_builder.response


class IntentReflectorHandler(AbstractRequestHandler):
    """The intent reflector is used for interaction model testing and debugging.
    It will simply repeat the intent the user said. You can create custom handlers
    for your intents by defining them above, then also adding them to the request
    handler chain below.
    """
    def can_handle(self, handler_input):
        # type: (HandlerInput) -> bool
        return ask_utils.is_request_type("IntentRequest")(handler_input)

    def handle(self, handler_input):
        # type: (HandlerInput) -> Response
        intent_name = ask_utils.get_intent_name(handler_input)
        speak_output = "You just triggered " + intent_name + "."

        return (
            handler_input.response_builder
                .speak(speak_output)
                # .ask("add a reprompt if you want to keep the session open for the user to respond")
                .response
        )


class CatchAllExceptionHandler(AbstractExceptionHandler):
    """Generic error handling to capture any syntax or routing errors. If you receive an error
    stating the request handler chain is not found, you have not implemented a handler for
    the intent being invoked or included it in the skill builder below.
    """
    def can_handle(self, handler_input, exception):
        # type: (HandlerInput, Exception) -> bool
        return True

    def handle(self, handler_input, exception):
        # type: (HandlerInput, Exception) -> Response
        logger.error(exception, exc_info=True)

        speak_output = "Sorry, I had trouble doing what you asked. Please try again."

        return (
            handler_input.response_builder
                .speak(speak_output)
                .ask(speak_output)
                .response
        )

# The SkillBuilder object acts as the entry point for your skill, routing all request and response
# payloads to the handlers above. Make sure any new handlers or interceptors you've
# defined are included below. The order matters - they're processed top to bottom.


sb = SkillBuilder()

sb.add_request_handler(LaunchRequestHandler())
sb.add_request_handler(HelloWorldIntentHandler())
sb.add_request_handler(MessageContentIntentHandler())
sb.add_request_handler(MessageWhoIntentHandler())
sb.add_request_handler(HelpIntentHandler())
sb.add_request_handler(CancelOrStopIntentHandler())
sb.add_request_handler(FallbackIntentHandler())
sb.add_request_handler(SessionEndedRequestHandler())
sb.add_request_handler(IntentReflectorHandler()) # make sure IntentReflectorHandler is last so it doesn't override your custom intent handlers

sb.add_exception_handler(CatchAllExceptionHandler())

lambda_handler = sb.lambda_handler()
