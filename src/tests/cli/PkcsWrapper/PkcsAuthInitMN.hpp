/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#pragma once

#include "PkcsAuthInit.hpp"
#include "impl/NativeWrapper.hpp"

using namespace System;

using namespace Apache::Geode::Client::Generic;

namespace Apache
{
  namespace Geode
  {
    namespace Client
    {
namespace Tests
        {
          namespace NewAPI
          {
          public ref class PkcsAuthInit sealed
          : public Internal::SBWrap<apache::geode::client::PKCSAuthInitInternal>,
            public Apache::Geode::Client::Generic::IAuthInitialize/*<String^, Object^>*/
        {
        public:
          
          PkcsAuthInit();          

          ~PkcsAuthInit();          

          //generic <class TPropKey, class TPropValue>
          virtual Apache::Geode::Client::Generic::Properties<String^, Object^> ^
            GetCredentials(
            Apache::Geode::Client::Generic::Properties<String^, String^>^ props, String^ server);

          virtual void Close();

        internal:            
          PkcsAuthInit( apache::geode::client::PKCSAuthInitInternal* nativeptr )
            : SBWrap( nativeptr ) { }
        };
        } // end namespace NewAPI
      }
    }
  }
}
