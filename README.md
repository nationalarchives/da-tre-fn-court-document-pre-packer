# da-tre-fn-template
A template repository with a skeleton scala handlers for a range of event types, a workflow for pushing images to ECR, and some simple examples of message parsing and testing.

The template lambda is generically typed to demonstrate handling of a selection of common events, but both event and return type can be configured as required.

## Testing

Sample feature and unit test for noop logging handlers are provided for each event type.

## Workflow

An on push workflow is provided which versions, tags, and pushes to ECR an image built from the latest code.

## Message parsing
Example parsing utils are supplied for `CourtDocumentPackage` : the precise circe codecs required will need adapting depending on types nested in parameters of the TRE message in question.
