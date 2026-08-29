# IceInsights frontend

Ionic 9 + Angular + Capacitor SPA. API client from OpenAPI via ng-openapi-gen.
Ionic components only. Dutch UI, ice-blue theme.

API-first: models and Angular services live in src/app/api/ generated
from openapi/openapi.yaml (ng-openapi-gen.json). Use the generate:api script.

Ionic 9 only. No second component kit.

Requirements: Node 22.22.3+, backend http://localhost:8086,
CORS for :8100 and :4200 in backend PR 11.

Screens: login, register, ritten, seizoenen, chips.
Auth guard and interceptor in src/app/core/.

TODOs: no laps endpoint; locationId/chipId without names;
SeasonTopEntry has no activity name; User has no password field.

Local: from this folder run the package install, then generate:api, then
ionic serve (port 8100) or the start script. Capacitor webDir is www.
Native platforms are not added here.

KPI cards show consecutive bests 1/2/4/8/13/25/50/100 as large tabular numbers.
